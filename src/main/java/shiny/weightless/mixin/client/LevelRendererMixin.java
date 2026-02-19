package shiny.weightless.mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.framegraph.FrameGraphBuilder;
import com.mojang.blaze3d.framegraph.FramePass;
import com.mojang.blaze3d.resource.GraphicsResourceAllocator;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LevelTargetBundle;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.client.renderer.state.LevelRenderState;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.Entity;
import org.joml.Matrix4f;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import shiny.weightless.client.WeightlessClient;
import shiny.weightless.client.render.WeightlessShaderHandler;
import shiny.weightless.client.render.RenderStateDataKeys;

@Mixin(LevelRenderer.class)
public class LevelRendererMixin {

    @Shadow @Final private Minecraft minecraft;
    @Shadow @Final private LevelTargetBundle targets;
    @Shadow @Final private LevelRenderState levelRenderState;

    @WrapOperation(method = "submitEntities", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/EntityRenderDispatcher;submit(Lnet/minecraft/client/renderer/entity/state/EntityRenderState;Lnet/minecraft/client/renderer/state/CameraRenderState;DDDLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;)V"))
    private void weightless$captureInWorld(EntityRenderDispatcher instance, EntityRenderState entityRenderState, CameraRenderState cameraRenderState, double d, double e, double f, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, Operation<Void> original) {
        if (Boolean.TRUE.equals(entityRenderState.getData(RenderStateDataKeys.IS_SHINY))) {
            entityRenderState.setData(RenderStateDataKeys.IN_WORLD, true);
        }
        original.call(instance, entityRenderState, cameraRenderState, d, e, f, poseStack, submitNodeCollector);
    }

    @Inject(method = "extractVisibleEntities", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/LevelRenderer;extractEntity(Lnet/minecraft/world/entity/Entity;F)Lnet/minecraft/client/renderer/entity/state/EntityRenderState;", shift = At.Shift.AFTER))
    private void weightless$extractShinyEntity(Camera camera, Frustum frustum, DeltaTracker deltaTracker, LevelRenderState levelRenderState, CallbackInfo ci, @Local(ordinal = 0) Entity entity) {
        if (entity.getUUID().equals(WeightlessClient.SHINY_UUID)) {
            levelRenderState.setData(RenderStateDataKeys.HAS_SHINY_ENTITY, true);
        }
    }

    @WrapOperation(method = "renderLevel", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/framegraph/FrameGraphBuilder;addPass(Ljava/lang/String;)Lcom/mojang/blaze3d/framegraph/FramePass;"))
    private FramePass weightless$loadShadersToFrame(FrameGraphBuilder fgb, String string, Operation<FramePass> original) {
        WeightlessShaderHandler.getInstance().loadToFrameGraphBuilder(fgb);
        return original.call(fgb, string);
    }

    @Inject(method = "addMainPass", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/framegraph/FramePass;executes(Ljava/lang/Runnable;)V"))
    private void weightless$updateReadWriteStatus(FrameGraphBuilder fgb, Frustum frustum, Matrix4f matrix4f, GpuBufferSlice slice, boolean bl, LevelRenderState state, DeltaTracker deltaTracker, ProfilerFiller profilerFiller, CallbackInfo ci, @Local(ordinal = 0) FramePass framePass) {
        WeightlessShaderHandler.getInstance().updateReadWriteStatus(state, framePass);
        WeightlessShaderHandler.getInstance().clear();
    }

    @Inject(method = "renderLevel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/LevelRenderer;addLateDebugPass(Lcom/mojang/blaze3d/framegraph/FrameGraphBuilder;Lnet/minecraft/client/renderer/state/CameraRenderState;Lcom/mojang/blaze3d/buffers/GpuBufferSlice;Lorg/joml/Matrix4f;)V"))
    private void weightless$drawShiny(GraphicsResourceAllocator graphicsResourceAllocator, DeltaTracker deltaTracker, boolean bl, Camera camera, Matrix4f matrix4f, Matrix4f matrix4f2, Matrix4f matrix4f3, GpuBufferSlice gpuBufferSlice, Vector4f vector4f, boolean bl2, CallbackInfo ci, @Local(ordinal = 0) FrameGraphBuilder fgb) {
        int i = this.minecraft.getMainRenderTarget().width;
        int j = this.minecraft.getMainRenderTarget().height;

        if (Boolean.TRUE.equals(this.levelRenderState.getData(RenderStateDataKeys.HAS_SHINY_ENTITY))) {
            WeightlessShaderHandler.getInstance().renderShinyShader(fgb, this.targets, i, j);
        }
    }
}
