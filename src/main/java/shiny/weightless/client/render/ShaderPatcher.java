package shiny.weightless.client.render;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.Std140Builder;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.resource.GraphicsResourceAllocator;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelTargetBundle;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.client.renderer.PostPass;
import net.minecraft.resources.Identifier;
import org.joml.Vector2f;
import shiny.weightless.common.Weightless;
import shiny.weightless.mixin.client.PostChainAccessor;
import shiny.weightless.mixin.client.PostPassAccessor;

import java.util.List;
import java.util.Map;

public class ShaderPatcher {

    private static final Identifier SPEED_LINES_ID = Weightless.id("speed_lines");
    private static final Identifier SPARKLE_ID = Weightless.id("sparkle");
    private static final String SPEED_LINES_BLOCK = "LinesConfig";
    private static final String SPARKLES_BLOCK = "SparkleConfig";

    private static GpuBuffer speedLinesUniformBuffer;
    private static GpuBuffer sparkleUniformBuffer;

    private static boolean setupBuffers() {
        boolean bl = false;
        if (speedLinesUniformBuffer == null || speedLinesUniformBuffer.isClosed()) {
            speedLinesUniformBuffer = RenderSystem.getDevice().createBuffer(
                    () -> "Speed lines uniforms buffer", GpuBuffer.USAGE_UNIFORM | GpuBuffer.USAGE_MAP_WRITE, 8
            );
            bl = true;
        }
        if (sparkleUniformBuffer == null || sparkleUniformBuffer.isClosed()) {
            sparkleUniformBuffer = RenderSystem.getDevice().createBuffer(
                    () -> "Sparkle uniforms buffer", GpuBuffer.USAGE_UNIFORM | GpuBuffer.USAGE_MAP_WRITE, 16
            );
            bl = true;
        }
        return bl;
    }

    public static void updateShaderUniforms() {
        if (setupBuffers()) {
            patchUniforms(
                    Minecraft.getInstance().getShaderManager().getPostChain(SPEED_LINES_ID, LevelTargetBundle.MAIN_TARGETS)
            );
            patchUniforms(
                    Minecraft.getInstance().getShaderManager().getPostChain(SPARKLE_ID, LevelTargetBundle.MAIN_TARGETS)
            );
        }
    }

    private static void patchUniforms(PostChain postChain) {
        List<PostPass> passes = ((PostChainAccessor) postChain).weightless$getPasses();
        for (PostPass pass : passes) {
            Map<String, GpuBuffer> uniforms = ((PostPassAccessor) pass).weightless$getUniforms();

            if (uniforms.containsKey(SPEED_LINES_BLOCK)) updateUniformBlock(uniforms, SPEED_LINES_BLOCK, speedLinesUniformBuffer);
            if (uniforms.containsKey(SPARKLES_BLOCK)) updateUniformBlock(uniforms, SPARKLES_BLOCK, sparkleUniformBuffer);
        }
    }

    private static void updateUniformBlock(Map<String, GpuBuffer> uniform, String key, GpuBuffer updated) {
        uniform.get(key).close();
        uniform.replace(key, updated);
    }

    private static PostChain getPostChain(Identifier id) {
        return Minecraft.getInstance().getShaderManager().getPostChain(id, LevelTargetBundle.MAIN_TARGETS);
    }

    public static void renderSpeedLines(GraphicsResourceAllocator gra, RenderTarget renderTarget, int worldTime, float speed) {
        PostChain postChain = getPostChain(SPEED_LINES_ID);
        if (postChain != null) {
            try (GpuBuffer.MappedView mappedView = RenderSystem.getDevice().createCommandEncoder().mapBuffer(speedLinesUniformBuffer, false, true)) {
                Std140Builder builder = Std140Builder.intoBuffer(mappedView.data());
                builder.putInt(worldTime).putFloat(speed);
            }
            postChain.process(renderTarget, gra);
        }
    }

    public static void renderSparkle(GraphicsResourceAllocator gra, RenderTarget renderTarget, Vector2f pos, float distance, int worldTime) {
        PostChain postChain = getPostChain(SPARKLE_ID);
        if (postChain != null) {
            try (GpuBuffer.MappedView mappedView = RenderSystem.getDevice().createCommandEncoder().mapBuffer(sparkleUniformBuffer, false, true)) {
                Std140Builder builder = Std140Builder.intoBuffer(mappedView.data());
                builder.putVec2(pos);
                builder.putFloat(distance);
                builder.putInt(worldTime);
            }
            postChain.process(renderTarget, gra);
        }
    }
}
