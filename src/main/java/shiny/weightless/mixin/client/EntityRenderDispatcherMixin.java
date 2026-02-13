package shiny.weightless.mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.state.CameraRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import shiny.weightless.client.util.EntityInGuiRenderExtension;

@Mixin(EntityRenderDispatcher.class)
public class EntityRenderDispatcherMixin implements EntityInGuiRenderExtension {

    @Unique private boolean isRenderedInGui;

    @WrapOperation(method = "submit", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/EntityRenderer;submit(Lnet/minecraft/client/renderer/entity/state/EntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/CameraRenderState;)V"))
    private <S extends EntityRenderState> void weightless$captureRenderedInGui(EntityRenderer renderer, S entityRenderState, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState cameraRenderState, Operation<Void> original) {
        original.call(renderer, entityRenderState, poseStack, submitNodeCollector, cameraRenderState);
        if (this.isRenderedInGui) {
            if (renderer instanceof EntityInGuiRenderExtension extension) extension.setRenderedInGui(true);
            this.isRenderedInGui = false;
        }
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
