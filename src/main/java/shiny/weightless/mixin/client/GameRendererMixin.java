package shiny.weightless.mixin.client;

import com.mojang.blaze3d.resource.CrossFrameResourcePool;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import shiny.weightless.client.WeightlessClient;
import shiny.weightless.client.render.SpeedLinesPatcher;
import shiny.weightless.common.component.WeightlessComponent;

@Mixin(GameRenderer.class)
public class GameRendererMixin {

    @Shadow @Final private Minecraft minecraft;
    @Shadow @Final private CrossFrameResourcePool resourcePool;

    @Inject(method = "resize", at = @At(value = "TAIL"))
    private void weightless$resizeShaderHandler(int i, int j, CallbackInfo ci) {
        WeightlessClient.getShaderHandler().resize(i, j);
    }

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/LevelRenderer;doEntityOutline()V", shift = At.Shift.AFTER))
    private void weightless$afterEntityOutlineRender(DeltaTracker deltaTracker, boolean bl, CallbackInfo ci) {
        WeightlessClient.getShaderHandler().copyToMainAfterOutline();

        if (this.minecraft.level != null && this.minecraft.player != null && WeightlessComponent.flying(this.minecraft.player)) {
            SpeedLinesPatcher.renderSpeedLines(
                    this.resourcePool,
                    this.minecraft.getMainRenderTarget(),
                    (int) this.minecraft.level.getGameTime(),
                    (float) Math.min(this.minecraft.player.getDeltaMovement().lengthSqr(), 1.0f)
            );
        }
    }
}
