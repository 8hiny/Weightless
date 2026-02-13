package shiny.weightless.mixin.client;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import shiny.weightless.client.util.EntityInGuiRenderExtension;
import shiny.weightless.client.util.RenderStateDataKeys;
import shiny.weightless.client.util.WeightlessPosing;
import shiny.weightless.client.render.WeightlessRenderTypes;

@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererMixin<T extends LivingEntity, S extends LivingEntityRenderState, M extends EntityModel<? super S>>
        extends EntityRenderer<T, S> implements EntityInGuiRenderExtension {

    @Shadow public abstract Identifier getTextureLocation(S livingEntityRenderState);
    @Shadow protected M model;
    @Unique RenderType shinyRenderType;
    @Unique boolean isRenderedInGui;

    protected LivingEntityRendererMixin(EntityRendererProvider.Context context) {
        super(context);
    }

    @Inject(method = "submit(Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/CameraRenderState;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/LivingEntityRenderer;getRenderType(Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;ZZZ)Lnet/minecraft/client/renderer/rendertype/RenderType;"))
    private void weightless$applyFlyingTransforms(S state, PoseStack stack, SubmitNodeCollector collector, CameraRenderState cameraState, CallbackInfo ci) {
        if (state instanceof HumanoidRenderState humanoidState) {
            if (Boolean.TRUE.equals(humanoidState.getData(RenderStateDataKeys.WEIGHTLESS_FLYING))) {
                WeightlessPosing.updateTransforms(stack, humanoidState.getData(RenderStateDataKeys.FLIGHT_TICKS), Minecraft.getInstance().getDeltaTracker().getRealtimeDeltaTicks());
            }
        }
    }

    //TODO Make gui rendering better (might not be compatible with modded gui's)
    @WrapMethod(method = "getRenderType")
    private @Nullable RenderType weightless$modifyRenderType(S state, boolean bl, boolean bl2, boolean bl3, Operation<RenderType> original) {
        if (Boolean.TRUE.equals(state.getData(RenderStateDataKeys.IS_SHINY))) {
            if (!this.isRenderedInGui) {
                this.shinyRenderType = WeightlessRenderTypes.getShinyPlaceholder(this.getTextureLocation(state), bl3);
            }
            else {
                this.shinyRenderType = null;
            }
            this.isRenderedInGui = false;
            return WeightlessRenderTypes.getEntityFullyEmissive(this.getTextureLocation(state), bl3);
        }
        return original.call(state, bl, bl2, bl3);
    }

    //TODO Fix z-fighting when walk animation is applied (very strange)
    @WrapOperation(method = "submit(Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/CameraRenderState;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/SubmitNodeCollector;submitModel(Lnet/minecraft/client/model/Model;Ljava/lang/Object;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/rendertype/RenderType;IIILnet/minecraft/client/renderer/texture/TextureAtlasSprite;ILnet/minecraft/client/renderer/feature/ModelFeatureRenderer$CrumblingOverlay;)V"))
    private void weightless$renderShinyPlaceholder(SubmitNodeCollector instance, Model model, Object object, PoseStack poseStack, RenderType renderType, int i, int j, int k, TextureAtlasSprite textureAtlasSprite, int l, ModelFeatureRenderer.CrumblingOverlay crumblingOverlay, Operation<Void> original) {
        if (this.shinyRenderType != null) {
            original.call(instance, model, object, poseStack, this.shinyRenderType, i, j, k, textureAtlasSprite, l, crumblingOverlay);
            this.shinyRenderType = null;
        }
        original.call(instance, model, object, poseStack, renderType, i, j, k, textureAtlasSprite, l, crumblingOverlay);
    }

    @Override
    public boolean isRenderedInGui() {
        return this.isRenderedInGui;
    }

    @Override
    public void setRenderedInGui(boolean value) {
        this.isRenderedInGui = value;
    }
}
