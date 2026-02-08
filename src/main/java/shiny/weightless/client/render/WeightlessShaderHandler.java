package shiny.weightless.client.render;

import com.mojang.blaze3d.framegraph.FrameGraphBuilder;
import com.mojang.blaze3d.framegraph.FramePass;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.resource.ResourceHandle;
import net.fabricmc.fabric.api.resource.v1.ResourceLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelTargetBundle;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.client.renderer.state.LevelRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import org.jetbrains.annotations.Nullable;
import shiny.weightless.client.util.RenderStateDataKeys;
import shiny.weightless.common.Weightless;

import java.util.Map;
import java.util.Set;

public class WeightlessShaderHandler implements ResourceManagerReloadListener {

    public static final Identifier RELOADER_ID = Weightless.id("weightless_shader_loader");
    public static final Identifier SHINY_TARGET_ID = Weightless.id("shiny");
    public static final Set<Identifier> SHINY_TARGETS = Set.of(LevelTargetBundle.MAIN_TARGET_ID, SHINY_TARGET_ID);

    private final Minecraft client;
    public ResourceHandle<RenderTarget> shinyHandle;
    private RenderTarget shinyTarget;

    public WeightlessShaderHandler(Minecraft client) {
        this.client = client;
        ResourceLoader.get(PackType.CLIENT_RESOURCES).registerReloader(RELOADER_ID, this);
    }

    @Override
    public void onResourceManagerReload(ResourceManager resourceManager) {
        if (this.shinyTarget != null) {
            this.shinyTarget.destroyBuffers();
        }
        this.shinyTarget = new TextureTarget("Shiny", this.client.getWindow().getWidth(), this.client.getWindow().getHeight(), true);
    }

    public void resize(int i, int j) {
        if (this.shinyTarget != null) {
            this.shinyTarget.resize(i, j);
        }
    }

    public void loadToFrameGraphBuilder(FrameGraphBuilder frameGraphBuilder) {
        if (this.shinyTarget != null) {
            this.shinyHandle = frameGraphBuilder.importExternal(SHINY_TARGET_ID.getPath(), this.shinyTarget);
        }
    }

    public void updateReadWriteStatus(LevelRenderState state, FramePass framePass) {
        if (Boolean.TRUE.equals(state.getData(RenderStateDataKeys.HAS_SHINY_ENTITY)) && this.shinyHandle != null) {
            this.shinyHandle = framePass.readsAndWrites(this.shinyHandle);
        }
    }

    public void copyToMain() {
        if (this.shinyTarget != null && this.client.player != null) {
            this.shinyTarget.blitAndBlendToTexture(this.client.getMainRenderTarget().getColorTextureView());
        }
    }

    public void renderShiny(Minecraft client, FrameGraphBuilder fgb, PostChain.TargetBundle targets, int screenWidth, int screenHeight) {
        PostChain postChain = client.getShaderManager().getPostChain(SHINY_TARGET_ID, SHINY_TARGETS);
        if (postChain != null) {
            postChain.addToFrame(fgb, screenWidth, screenHeight, new DynamicTargetBundle(targets, Map.of(SHINY_TARGET_ID, shinyHandle)));
        }
    }

    @Nullable
    public RenderTarget shinyTarget() {
        return this.shinyHandle != null ? this.shinyHandle.get() : null;
    }
}
