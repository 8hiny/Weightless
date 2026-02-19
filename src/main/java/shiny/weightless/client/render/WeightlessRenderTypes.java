package shiny.weightless.client.render;

import net.minecraft.client.renderer.rendertype.OutputTarget;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.resources.Identifier;

public class WeightlessRenderTypes {

    //OutputTargets
    private static final OutputTarget SHINY_TARGET = new OutputTarget("shiny_target", () -> WeightlessShaderHandler.getInstance().shinyTarget());

    public static RenderType getEntityFullyEmissive(Identifier id, boolean bloom, boolean affectsOutline) {
        RenderSetup renderSetup = RenderSetup.builder(WeightlessRenderPipelines.ENTITY_FULLY_EMISSIVE)
                .withTexture("Sampler0", id)
                .useOverlay()
                .affectsCrumbling()
                .sortOnUpload()
                .setOutline(affectsOutline ? RenderSetup.OutlineProperty.AFFECTS_OUTLINE : RenderSetup.OutlineProperty.NONE)
                .setOutputTarget(bloom ? SHINY_TARGET : OutputTarget.MAIN_TARGET)
                .createRenderSetup();

        return RenderType.create("entity_fully_emissive", renderSetup);
    }
}
