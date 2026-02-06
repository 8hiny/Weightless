package shiny.weightless.mixin.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.util.Mth;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import shiny.weightless.client.model.RenderStateDataKeys;
import shiny.weightless.client.model.WeightlessPosing;

@Mixin(PlayerModel.class)
public abstract class PlayerModelMixin extends HumanoidModel<AvatarRenderState> {

    @Unique private float lastLimbPos;

    public PlayerModelMixin(ModelPart modelPart) {
        super(modelPart);
    }

    @Inject(method = "setupAnim(Lnet/minecraft/client/renderer/entity/state/AvatarRenderState;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/model/HumanoidModel;setupAnim(Lnet/minecraft/client/renderer/entity/state/HumanoidRenderState;)V"))
    private void weightless$updateLimbAngle(AvatarRenderState renderState, CallbackInfo ci) {
        if (Boolean.TRUE.equals(renderState.getData(RenderStateDataKeys.WEIGHTLESS_FLYING))) {
            renderState.walkAnimationPos = Mth.lerp(0.01f, this.lastLimbPos, 0.0f);
            this.lastLimbPos = renderState.walkAnimationPos;
        }
    }

    @Inject(method = "setupAnim(Lnet/minecraft/client/renderer/entity/state/AvatarRenderState;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/model/HumanoidModel;setupAnim(Lnet/minecraft/client/renderer/entity/state/HumanoidRenderState;)V", shift = At.Shift.AFTER))
    private void weightless$applyFlightPose(AvatarRenderState renderState, CallbackInfo ci) {
        if (Boolean.TRUE.equals(renderState.getData(RenderStateDataKeys.WEIGHTLESS_FLYING)) && !renderState.isCrouching) {
            Minecraft client = Minecraft.getInstance();
            boolean firstPerson = client.options.getCameraType().isFirstPerson();
            if (!Boolean.TRUE.equals(renderState.getData(RenderStateDataKeys.IS_LOCAL_PLAYER)) || !firstPerson) {
                WeightlessPosing.setAngles(this, renderState, client.getDeltaTracker().getRealtimeDeltaTicks());
            }
        }
    }
}
