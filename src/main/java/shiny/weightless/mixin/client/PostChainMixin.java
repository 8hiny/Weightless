package shiny.weightless.mixin.client;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.client.renderer.PostPass;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import shiny.weightless.client.util.DynamicPostPass;
import shiny.weightless.common.Weightless;

import java.util.List;
import java.util.Map;

@Mixin(PostChain.class)
public class PostChainMixin {

    @Redirect(method = "createPass", at = @At(value = "NEW", target = "(Lcom/mojang/blaze3d/pipeline/RenderPipeline;Lnet/minecraft/resources/Identifier;Ljava/util/Map;Ljava/util/List;)Lnet/minecraft/client/renderer/PostPass;"))
    private static PostPass weightless$createDynamicPass(RenderPipeline renderPipeline, Identifier identifier, Map map, List list) {
        if (renderPipeline.getLocation().getNamespace().equals("weightless")) {
            return new DynamicPostPass(renderPipeline, identifier, map, list);
        }
        return new PostPass(renderPipeline, identifier, map, list);
    }
}
