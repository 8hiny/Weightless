package shiny.weightless.mixin.client;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.util.Mth;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import shiny.weightless.client.util.RenderStateDataKeys;

@Mixin(HumanoidModel.class)
public abstract class HumanoidModelMixin<T extends HumanoidRenderState> extends EntityModel<T> {

    @Unique private float lastWalkSpeed;

    protected HumanoidModelMixin(ModelPart modelPart) {
        super(modelPart);
    }

    @ModifyVariable(method = "setupAnim(Lnet/minecraft/client/renderer/entity/state/HumanoidRenderState;)V", at = @At(value = "STORE"), ordinal = 2)
    private float weightless$overrideWalkAnimation(float h, @Local(argsOnly = true) HumanoidRenderState state) {
        if (!state.isCrouching && Boolean.TRUE.equals(state.getData(RenderStateDataKeys.WEIGHTLESS_FLYING))) {
            h = Mth.lerp(0.01f, this.lastWalkSpeed, 0.0f);
        }
        this.lastWalkSpeed = h;
        return h;
    }
}
