package shiny.weightless.client.render;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.RenderPipelines;
import shiny.weightless.common.Weightless;

public class WeightlessRenderPipelines {

    //Snippets (currently unused)
    public static final RenderPipeline.Snippet CUSTOM_ENTITY_SNIPPET = RenderPipeline.builder(RenderPipelines.MATRICES_FOG_SNIPPET)
            .withVertexShader(Weightless.id("core/entity_custom"))
            .withFragmentShader(Weightless.id("core/entity_custom"))
            .withSampler("Sampler0")
            .withVertexFormat(DefaultVertexFormat.NEW_ENTITY, VertexFormat.Mode.QUADS)
            .buildSnippet();

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
