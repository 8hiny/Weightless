package shiny.weightless.client.util;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.resource.ResourceHandle;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class DynamicTargetBundle implements PostChain.TargetBundle {

    private final PostChain.TargetBundle delegate;
    private final Map<Identifier, ResourceHandle<RenderTarget>> targets;

    public DynamicTargetBundle(PostChain.TargetBundle delegate, Map<Identifier, ResourceHandle<RenderTarget>> targets) {
        this.delegate = delegate;
        this.targets = new HashMap<>(targets);
    }

    @Override
    public void replace(Identifier identifier, ResourceHandle<RenderTarget> resourceHandle) {
        if (this.targets.containsKey(identifier)) {
            this.targets.replace(identifier, resourceHandle);
        }
        else {
            this.delegate.replace(identifier, resourceHandle);
        }
    }

    @Override
    public @Nullable ResourceHandle<RenderTarget> get(Identifier identifier) {
        if (this.targets.containsKey(identifier)) {
            return this.targets.get(identifier);
        }
        return this.delegate.get(identifier);
    }
}
