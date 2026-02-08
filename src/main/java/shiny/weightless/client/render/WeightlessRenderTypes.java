package shiny.weightless.client.render;

import net.minecraft.client.renderer.rendertype.OutputTarget;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Util;
import shiny.weightless.client.WeightlessClient;

import java.util.function.BiFunction;

public class WeightlessRenderTypes {

    //OutputTargets
    private static final OutputTarget SHINY_TARGET = new OutputTarget("shiny_target", () -> WeightlessClient.getShaderHandler().shinyTarget());

    //RenderTypes
    private static final BiFunction<Identifier, Boolean, RenderType> ENTITY_FULLY_EMISSIVE = Util.memoize(
            (identifier, boolean_) -> {
                RenderSetup renderSetup = RenderSetup.builder(WeightlessRenderPipelines.ENTITY_FULLY_EMISSIVE)
                        .withTexture("Sampler0", identifier)
                        .useOverlay()
                        .affectsCrumbling()
                        .sortOnUpload()
                        .setOutline(boolean_ ? RenderSetup.OutlineProperty.AFFECTS_OUTLINE : RenderSetup.OutlineProperty.NONE)
                        .setOutputTarget(SHINY_TARGET)
                        .createRenderSetup();
                return RenderType.create("entity_fully_emissive", renderSetup);
            }
    );

    public static RenderType getEntityFullyEmissive(Identifier id, boolean affectsOutline) {
        return ENTITY_FULLY_EMISSIVE.apply(id, affectsOutline);
    }
}
