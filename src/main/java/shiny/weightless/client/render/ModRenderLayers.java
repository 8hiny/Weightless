package shiny.weightless.client.render;

import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderPhase;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;

import java.util.function.BiFunction;

public class ModRenderLayers extends RenderLayer {

    public static final BiFunction<Identifier, Boolean, RenderLayer> TRAIL = Util.memoize(
            (texture, affectsOutline) -> {
                RenderLayer.MultiPhaseParameters multiPhaseParameters = RenderLayer.MultiPhaseParameters.builder()
                        .program(RenderPhase.ENTITY_TRANSLUCENT_CULL_PROGRAM)
                        .texture(new Texture(texture, false, false))
                        .transparency(TRANSLUCENT_TRANSPARENCY)
                        .writeMaskState(ALL_MASK)
                        .cull(DISABLE_CULLING)
                        .build(affectsOutline);
                return RenderLayer.of(
                        "weightless:trail", VertexFormats.POSITION_COLOR_TEXTURE_LIGHT, VertexFormat.DrawMode.QUADS, 256, true, true, multiPhaseParameters
                );
            }
    );

    public ModRenderLayers(String name, VertexFormat vertexFormat, VertexFormat.DrawMode drawMode, int expectedBufferSize, boolean hasCrumbling, boolean translucent, Runnable startAction, Runnable endAction) {
        super(name, vertexFormat, drawMode, expectedBufferSize, hasCrumbling, translucent, startAction, endAction);
    }

    public static RenderLayer getTrail(Identifier texture) {
        return TRAIL.apply(texture, false);
    }
}
