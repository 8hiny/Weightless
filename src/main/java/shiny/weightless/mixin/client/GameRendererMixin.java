package shiny.weightless.mixin.client;

import com.mojang.blaze3d.resource.CrossFrameResourcePool;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelTargetBundle;
import net.minecraft.client.renderer.PostChain;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import shiny.weightless.common.Weightless;
import shiny.weightless.common.component.WeightlessComponent;
import shiny.weightless.common.config.ModConfig;

@Mixin(GameRenderer.class)
public class GameRendererMixin {

    @Shadow @Final private Minecraft minecraft;
    @Shadow @Final private CrossFrameResourcePool resourcePool;

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/LevelRenderer;doEntityOutline()V", shift = At.Shift.AFTER))
    private void weightless$renderPostShader(DeltaTracker deltaTracker, boolean bl, CallbackInfo ci) {
        if (ModConfig.renderSpeedlines && this.minecraft.player != null && WeightlessComponent.flying(this.minecraft.player)) {
            PostChain postChain = this.minecraft.getShaderManager().getPostChain(Weightless.id("speed_lines"), LevelTargetBundle.MAIN_TARGETS);
            if (postChain != null) {
                postChain.process(this.minecraft.getMainRenderTarget(), this.resourcePool);
            }
        }
    }
}
