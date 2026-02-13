package shiny.weightless.client.render;

import net.minecraft.client.renderer.rendertype.OutputTarget;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Util;

import java.util.function.BiFunction;

public class WeightlessRenderTypes {

    //OutputTargets
    private static final OutputTarget SHINY_TARGET = new OutputTarget("shiny_target", () -> WeightlessShaderHandler.getInstance().shinyTarget());

    //RenderTypes
    private static final BiFunction<Identifier, Boolean, RenderType> SHINY_PLACEHOLDER = Util.memoize(
            (id, affectsOutline) -> {
                RenderSetup renderSetup = RenderSetup.builder(WeightlessRenderPipelines.SHINY_PLACEHOLDER)
                        .withTexture("Sampler0", id)
                        .setOutline(affectsOutline ? RenderSetup.OutlineProperty.AFFECTS_OUTLINE : RenderSetup.OutlineProperty.NONE)
                        .sortOnUpload()
                        .createRenderSetup();
                return RenderType.create("shiny_placeholder", renderSetup);
            }
    );
    private static final BiFunction<Identifier, Boolean, RenderType> ENTITY_FULLY_EMISSIVE = Util.memoize(
            (id, affectsOutline) -> {
                RenderSetup renderSetup = RenderSetup.builder(WeightlessRenderPipelines.ENTITY_FULLY_EMISSIVE)
                        .withTexture("Sampler0", id)
                        .useOverlay()
                        .affectsCrumbling()
                        .sortOnUpload()
                        .setOutline(affectsOutline ? RenderSetup.OutlineProperty.AFFECTS_OUTLINE : RenderSetup.OutlineProperty.NONE)
                        .setOutputTarget(SHINY_TARGET)
                        .createRenderSetup();
                return RenderType.create("entity_fully_emissive", renderSetup);
            }
    );

    public static RenderType getShinyPlaceholder(Identifier id, boolean affectsOutline) {
        return SHINY_PLACEHOLDER.apply(id, affectsOutline);
    }

    public static RenderType getEntityFullyEmissive(Identifier id, boolean affectsOutline) {
        return ENTITY_FULLY_EMISSIVE.apply(id, affectsOutline);
    }
}
