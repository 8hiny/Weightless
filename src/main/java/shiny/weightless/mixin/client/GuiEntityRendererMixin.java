package shiny.weightless.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.render.pip.GuiEntityRenderer;
import net.minecraft.client.gui.render.state.pip.GuiEntityRenderState;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import shiny.weightless.client.util.EntityInGuiRenderExtension;

@Mixin(GuiEntityRenderer.class)
public abstract class GuiEntityRendererMixin {

    @Shadow @Final private EntityRenderDispatcher entityRenderDispatcher;

    @Inject(method = "renderToTexture(Lnet/minecraft/client/gui/render/state/pip/GuiEntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;)V", at = @At(value = "HEAD"))
    private void weightless$captureRenderedInGui(GuiEntityRenderState guiEntityRenderState, PoseStack poseStack, CallbackInfo ci) {
        if (this.entityRenderDispatcher instanceof EntityInGuiRenderExtension extension) {
            extension.setRenderedInGui(true);
        }
    }
}
