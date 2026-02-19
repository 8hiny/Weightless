package shiny.weightless.client.render;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import net.minecraft.client.renderer.RenderPipelines;

public class WeightlessRenderPipelines {

    //Pipelines
    public static final RenderPipeline ENTITY_FULLY_EMISSIVE = RenderPipelines.register(
            RenderPipeline.builder(RenderPipelines.ENTITY_EMISSIVE_SNIPPET)
                    .withLocation("pipeline/entity_fully_emissive")
                    .withShaderDefine("ALPHA_CUTOUT", 0.1f)
                    .withShaderDefine("NO_CARDINAL_LIGHTING")
                    .withSampler("Sampler1")
                    .build()
    );
}
