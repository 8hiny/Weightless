package shiny.weightless.mixin.client;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import shiny.weightless.client.RenderStateDataKeys;
import shiny.weightless.client.util.WeightlessPosing;

@Mixin(HumanoidModel.class)
public abstract class HumanoidModelMixin<T extends HumanoidRenderState> extends EntityModel<T> {

    protected HumanoidModelMixin(ModelPart modelPart) {
        super(modelPart);
    }

    @ModifyVariable(method = "setupAnim(Lnet/minecraft/client/renderer/entity/state/HumanoidRenderState;)V", at = @At(value = "STORE"), ordinal = 2)
    private float overrideWalkAnimation(float h, @Local(argsOnly = true) HumanoidRenderState state) {
        if (!state.isCrouching && Boolean.TRUE.equals(state.getData(RenderStateDataKeys.WEIGHTLESS_FLYING))) {
            h = 0.0f;
        }
        return h;
    }

    @Inject(method = "setupAnim(Lnet/minecraft/client/renderer/entity/state/HumanoidRenderState;)V", at = @At(value = "TAIL"))
    private void applyFlightAnimation(T state, CallbackInfo ci) {
        if (!state.isCrouching
                && Boolean.TRUE.equals(state.getData(RenderStateDataKeys.WEIGHTLESS_FLYING))
                && !(Boolean.TRUE.equals(state.getData(RenderStateDataKeys.IS_LOCAL_PLAYER))
                        && Minecraft.getInstance().options.getCameraType().isFirstPerson())
        ) {
            WeightlessPosing.setAngles((HumanoidModel<T>) (Object) this, state);
        }
    }
}
