package shiny.weightless.mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import shiny.weightless.WeightlessRenderProvider;
import shiny.weightless.client.render.WeightlessPosing;
import shiny.weightless.common.component.WeightlessComponent;

@Mixin(PlayerEntityModel.class)
public class PlayerEntityModelMixin<T extends LivingEntity> extends BipedEntityModel<T> {

    public PlayerEntityModelMixin(ModelPart root) {
        super(root);
    }

    @WrapOperation(method = "setAngles(Lnet/minecraft/entity/LivingEntity;FFFFF)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/entity/model/BipedEntityModel;setAngles(Lnet/minecraft/entity/LivingEntity;FFFFF)V"))
    private void weightless$preventLimbMovement(PlayerEntityModel<T> model, T entity, float limbAngle, float limbDistance, float animationProgress, float headYaw, float headPitch, Operation<Void> original) {
        if (entity instanceof WeightlessRenderProvider provider) {
            if (entity instanceof PlayerEntity player && WeightlessComponent.flying(player) && !player.isSneaking()) {
                limbDistance = MathHelper.lerp(0.01f, provider.getLastLimbAngle(), 0.0f);
            }
            provider.setLastLimbAngle(limbDistance);
        }
        original.call(model, entity, limbAngle, limbDistance, animationProgress, headYaw, headPitch);
    }

    @Inject(method = "setAngles(Lnet/minecraft/entity/LivingEntity;FFFFF)V", at = @At(value = "TAIL"))
    private void weightless$applyTransforms(T entity, float limbAngle, float limbDistance, float animationProgress, float headYaw, float headPitch, CallbackInfo ci) {
        if (entity instanceof PlayerEntity player && WeightlessComponent.has(player) && entity instanceof WeightlessRenderProvider provider) {
            if (WeightlessComponent.flying(player) && !player.isSneaking()) {
                boolean bl = MinecraftClient.getInstance().options.getPerspective().isFirstPerson();
                if (player != MinecraftClient.getInstance().player || !bl) {
                    PlayerEntityModel<?> model = (PlayerEntityModel<?>) (Object) this;
                    WeightlessPosing.setAngles(model, player, animationProgress);
                }
            }
        }
    }
}
