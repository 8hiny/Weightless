package shiny.weightless.client.util;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.buffers.Std140Builder;
import com.mojang.blaze3d.buffers.Std140SizeCalculator;
import com.mojang.blaze3d.framegraph.FrameGraphBuilder;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.resource.ResourceHandle;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.PostPass;
import net.minecraft.client.renderer.UniformValue;
import net.minecraft.resources.Identifier;
import org.lwjgl.system.MemoryStack;
import shiny.weightless.common.component.WeightlessComponent;
import shiny.weightless.mixin.client.PostPassAccessor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

//TODO Need to somehow create a custom PostPass which rewrites its uniforms before rendering
public class DynamicPostPass extends PostPass {

    private static final String SPEED_LINES_UNIFORM = "LinesConfig";
    private final List<UniformValue> uniforms;

    public DynamicPostPass(RenderPipeline renderPipeline, Identifier identifier, Map<String, List<UniformValue>> map, List<Input> list) {
        super(renderPipeline, identifier, map, list);
        if (map.containsKey(SPEED_LINES_UNIFORM)) {
            this.uniforms = new ArrayList<>(map.get(SPEED_LINES_UNIFORM));
        }
        else {
            this.uniforms = List.of();
        }
    }

    //This is real 2:12am bs right here - Shiny, at 2:12am on 06.02.2026 when he should've BEEN ASLEEP that IDIOT
    //I have no clue whether this here is a smart idea or not; There is most definitely some sort of performance loss here lmfao
    @Override
    public void addToFrame(FrameGraphBuilder frameGraphBuilder, Map<Identifier, ResourceHandle<RenderTarget>> map, GpuBufferSlice gpuBufferSlice) {
        Minecraft client = Minecraft.getInstance();
        if (client.level != null && client.player != null & WeightlessComponent.flying(client.player)) {
            for (Map.Entry<String, GpuBuffer> entry : ((PostPassAccessor) this).weightless$getCustomUniforms().entrySet()) {
                if (entry.getKey().equals(SPEED_LINES_UNIFORM)) {

                    Std140SizeCalculator std140SizeCalculator = new Std140SizeCalculator();
                    for (int i = 0; i < this.uniforms.size(); i++) {
                        UniformValue value = this.uniforms.get(i);
                        if (value instanceof UniformValue.IntUniform) {
                            this.uniforms.set(i, new UniformValue.IntUniform((int) client.level.getGameTime()));
                        }
                        else if (value instanceof UniformValue.FloatUniform) {
                            this.uniforms.set(i, new UniformValue.FloatUniform((float) Math.min(client.player.getDeltaMovement().lengthSqr(), 1.0f)));
                        }

                        this.uniforms.get(i).addSize(std140SizeCalculator);
                    }
                    int i = std140SizeCalculator.get();

                    try (MemoryStack memoryStack = MemoryStack.stackPush()) {
                        Std140Builder std140Builder = Std140Builder.onStack(memoryStack, i);

                        for (UniformValue uniformValue2 : uniforms) {
                            uniformValue2.writeTo(std140Builder);
                        }
                        entry.setValue(RenderSystem.getDevice().createBuffer(() -> ((PostPassAccessor) this).weightless$getName() + " / " + entry.getKey(), 128, std140Builder.get()));
                    }
                }
            }
        }
        super.addToFrame(frameGraphBuilder, map, gpuBufferSlice);
    }
}
