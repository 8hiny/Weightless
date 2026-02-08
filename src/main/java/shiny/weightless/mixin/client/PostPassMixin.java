package shiny.weightless.mixin.client;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.framegraph.FrameGraphBuilder;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.resource.ResourceHandle;
import net.minecraft.client.renderer.PostPass;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import shiny.weightless.common.Weightless;

import java.util.Map;

@Mixin(PostPass.class)
public class PostPassMixin {

    @Shadow @Final private String name;

    @Inject(method = "addToFrame", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/PostPass$Input;addToPass(Lcom/mojang/blaze3d/framegraph/FramePass;Ljava/util/Map;)V"))
    private void weightless$test(FrameGraphBuilder frameGraphBuilder, Map<Identifier, ResourceHandle<RenderTarget>> map, GpuBufferSlice gpuBufferSlice, CallbackInfo ci, @Local(ordinal = 0) PostPass.Input input) {
        Weightless.LOGGER.info("Post pass " + this.name + " input " + input + ": " + map);
    }
}
