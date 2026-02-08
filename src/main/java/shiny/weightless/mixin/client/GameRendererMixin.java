package shiny.weightless.mixin.client;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import shiny.weightless.client.WeightlessClient;

@Mixin(GameRenderer.class)
public class GameRendererMixin {

    @Inject(method = "resize", at = @At(value = "TAIL"))
    private void weightless$resizeShaderHandler(int i, int j, CallbackInfo ci) {
        WeightlessClient.getShaderHandler().resize(i, j);
    }

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/LevelRenderer;doEntityOutline()V", shift = At.Shift.AFTER))
    private void weightless$blendBufferToMain(DeltaTracker deltaTracker, boolean bl, CallbackInfo ci) {
        WeightlessClient.getShaderHandler().copyToMain();
    }
}
