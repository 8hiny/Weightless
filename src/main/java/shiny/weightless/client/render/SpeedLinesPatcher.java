package shiny.weightless.client.render;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.Std140Builder;
import com.mojang.blaze3d.framegraph.FrameGraphBuilder;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelTargetBundle;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.client.renderer.PostPass;
import shiny.weightless.common.Weightless;
import shiny.weightless.mixin.client.PostChainAccessor;
import shiny.weightless.mixin.client.PostPassAccessor;

import java.util.List;
import java.util.Map;

public class SpeedLinesPatcher {

    private static final String UNIFORM_BLOCK = "LinesConfig";
    private static final GpuBuffer linesBuffer = RenderSystem.getDevice().createBuffer(
            () -> "Speed lines config buffer", GpuBuffer.USAGE_UNIFORM | GpuBuffer.USAGE_MAP_WRITE, 8
    );
    private static final PostChain speedLines = patchUniforms(
            Minecraft.getInstance().getShaderManager().getPostChain(Weightless.id("speed_lines"), LevelTargetBundle.MAIN_TARGETS)
    );

    private static PostChain patchUniforms(PostChain postChain) {
        List<PostPass> passes = ((PostChainAccessor) postChain).weightless$getPasses();
        for (PostPass pass : passes) {
            Map<String, GpuBuffer> uniforms = ((PostPassAccessor) pass).weightless$getUniforms();
            if (uniforms.containsKey(UNIFORM_BLOCK)) {
                uniforms.get(UNIFORM_BLOCK).close();
                uniforms.put(UNIFORM_BLOCK, linesBuffer);
            }
        }
        return postChain;
    }

    public static void renderSpeedLines(FrameGraphBuilder fgb, PostChain.TargetBundle targets, int screenWidth, int screenHeight, int worldTime, float speed) {
        try (GpuBuffer.MappedView mappedView = RenderSystem.getDevice().createCommandEncoder().mapBuffer(linesBuffer, false, true)) {
            Std140Builder builder = Std140Builder.intoBuffer(mappedView.data());
            builder.putInt(worldTime).putFloat(speed);
        }
        speedLines.addToFrame(fgb, screenWidth, screenHeight, targets);
    }
}
