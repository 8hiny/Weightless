package shiny.weightless.mixin.client;

import com.mojang.blaze3d.resource.CrossFrameResourcePool;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector2f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import shiny.weightless.client.WeightlessClient;
import shiny.weightless.client.render.ShaderPatcher;
import shiny.weightless.client.render.WeightlessShaderHandler;
import shiny.weightless.common.component.WeightlessComponent;
import shiny.weightless.common.config.ModConfig;

@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {

    @Shadow @Final private Minecraft minecraft;
    @Shadow @Final private CrossFrameResourcePool resourcePool;
    @Shadow @Final private Camera mainCamera;

    @Shadow public abstract Vec3 projectPointToScreen(Vec3 vec3);

    //Runs right after the world is rendered as to avoid overriding hand rendering
    @Inject(method = "renderLevel", at = @At(value = "INVOKE_STRING", target = "Lnet/minecraft/util/profiling/ProfilerFiller;popPush(Ljava/lang/String;)V", args = "ldc=hand"))
    private void weightless$postLevelRender(DeltaTracker deltaTracker, CallbackInfo ci) {
        WeightlessShaderHandler.getInstance().blitToMainBuffer();

        if (this.minecraft.isGameLoadFinished()) {
            ShaderPatcher.updateShaderUniforms();

            if (this.minecraft.level != null && this.minecraft.player != null) {
                Player shiny = this.minecraft.level.getPlayerByUUID(WeightlessClient.SHINY_UUID);
                if (shiny != null && shiny != this.minecraft.player && !shiny.isInvisibleTo(this.minecraft.player) && shiny.canBeSeenByAnyone()) {
                    float tickDelta = deltaTracker.getGameTimeDeltaPartialTick(true);
                    Vec3 pos = shiny.getPosition(tickDelta).add(new Vec3(0, shiny.getBbHeight() * 0.65, 0));
                    Vec3 clientPos = this.mainCamera.position();
                    Vec3 screenPos = this.projectPointToScreen(pos);
                    Vec3 dir = pos.subtract(clientPos);

                    HitResult hitResult = this.minecraft.level.clipIncludingBorder(new ClipContext(clientPos, pos, ClipContext.Block.VISUAL, ClipContext.Fluid.NONE, this.minecraft.player));
                    if (hitResult.getType().equals(HitResult.Type.MISS)) {
                        float dot = (float) shiny.calculateViewVector(this.mainCamera.xRot(), this.mainCamera.yRot()).dot(dir);
                        if (screenPos.x >= -1.0f && screenPos.x <= 1.0f && screenPos.y >= -1.0f && screenPos.y <= 1.0f && dot > 0.0f) {
                            float distance = (float) pos.distanceTo(clientPos);
                            if (distance > 0.0f) {
                                ShaderPatcher.renderSparkle(
                                        this.resourcePool,
                                        this.minecraft.getMainRenderTarget(),
                                        new Vector2f((float) screenPos.x, (float) screenPos.y),
                                        Math.clamp(distance / 128.0f, 0.3f, 1.0f),
                                        (int) this.minecraft.level.getGameTime()
                                );
                            }
                        }
                    }
                }
            }
        }
    }

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/LevelRenderer;doEntityOutline()V", shift = At.Shift.AFTER))
    private void weightless$postEntityOutlineRender(DeltaTracker deltaTracker, boolean bl, CallbackInfo ci) {
        if (ModConfig.renderSpeedlines && this.minecraft.level != null && this.minecraft.player != null) {
            if (WeightlessComponent.flying(this.minecraft.player)) {
                ShaderPatcher.renderSpeedLines(
                        this.resourcePool,
                        this.minecraft.getMainRenderTarget(),
                        (int) this.minecraft.level.getGameTime(),
                        (float) Math.min(this.minecraft.player.getDeltaMovement().lengthSqr(), 1.0f)
                );
            }
        }
    }

    @Inject(method = "resize", at = @At(value = "TAIL"))
    private void weightless$resizeShaderHandler(int i, int j, CallbackInfo ci) {
        WeightlessShaderHandler.getInstance().resize(i, j);
    }
}
