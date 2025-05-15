package shiny.weightless.mixin.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import shiny.weightless.client.render.WeightlessPosing;
import shiny.weightless.common.component.WeightlessComponent;

@Mixin(LivingEntityRenderer.class)
public class LivingEntityRendererMixin<T extends LivingEntity, M extends EntityModel<T>> {

    @Shadow protected M model;

    @Inject(method = "render(Lnet/minecraft/entity/LivingEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/entity/model/EntityModel;setAngles(Lnet/minecraft/entity/Entity;FFFFF)V", shift = At.Shift.AFTER))
    private void weightless$flyingTransforms(T entity, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumerProvider, int i, CallbackInfo ci) {
        if (entity instanceof AbstractClientPlayerEntity player && this.model instanceof PlayerEntityModel<?> playerModel) {
            if (!player.isSneaking() && WeightlessComponent.flying(player)) {
                WeightlessPosing.updateTransforms(matrices, player, tickDelta);

                boolean bl = MinecraftClient.getInstance().options.getPerspective().isFirstPerson();
                if (player != MinecraftClient.getInstance().player || !bl) {
                    WeightlessPosing.setAngles(playerModel, player, tickDelta);
                }
            }
        }
    }
}
