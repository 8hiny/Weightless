package shiny.weightless;

import ladysnake.satin.api.event.ShaderEffectRenderCallback;
import ladysnake.satin.api.managed.ManagedShaderEffect;
import ladysnake.satin.api.managed.ShaderEffectManager;
import ladysnake.satin.api.managed.uniform.Uniform1f;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.DefaultParticleType;
import net.minecraft.particle.ParticleType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;
import org.joml.Vector3i;
import shiny.weightless.client.particle.ColorParticleEffect;
import shiny.weightless.client.particle.PointParticle;
import shiny.weightless.client.particle.ShockwaveParticle;
import shiny.weightless.client.sound.WeightlessFlyingSoundInstance;
import shiny.weightless.client.trail.Trail;
import shiny.weightless.client.trail.TrailRenderer;
import shiny.weightless.common.component.WeightlessComponent;

import java.awt.*;

public class WeightlessClient implements ClientModInitializer {

    //Particles
    public static final DefaultParticleType SHOCKWAVE = Registry.register(Registries.PARTICLE_TYPE, Weightless.id("shockwave"), FabricParticleTypes.simple());
    public static final ParticleType<ColorParticleEffect> POINT = Registry.register(
            Registries.PARTICLE_TYPE,
            Weightless.id("point"),
            FabricParticleTypes.complex(true, ColorParticleEffect.PARAMETERS_FACTORY)
    );

    //Speed lines shader & uniforms
    public static final ManagedShaderEffect SPEED_LINES = ShaderEffectManager.getInstance().manage(Weightless.id("shaders/post/speed_lines.json"));
    public static final Uniform1f WORLD_TIME = SPEED_LINES.findUniform1f("WorldTime");
    public static final Uniform1f FLY_SPEED = SPEED_LINES.findUniform1f("FlySpeed");
    private static long worldTime = 0;
    private static float flySpeed = 0.0f;
    private static boolean flying = false;

    //Keybinds
    public static KeyBinding TOGGLE_WEIGHTLESS = KeyBindingHelper.registerKeyBinding(new KeyBinding("keybind.weightless.toggle",InputUtil.UNKNOWN_KEY.getCode(), "key.categories.weightless"));
    public static KeyBinding AUTOPILOT = KeyBindingHelper.registerKeyBinding(new KeyBinding("keybind.weightless.autopilot", InputUtil.UNKNOWN_KEY.getCode(), "key.categories.weightless"));
    public static boolean wasWeightlessPressed = false;
    public static boolean wasAutopilotPressed = false;
    public static boolean weightlessActive = true;
    public static boolean autopilotActive = false;

    //Disconnect message for config mismatch
    public static final Text DISCONNECT_MESSAGE = Text.translatable("message.weightless.disconnect");

    @Override
    public void onInitializeClient() {
        ParticleFactoryRegistry.getInstance().register(SHOCKWAVE, ShockwaveParticle.Factory::new);
        ParticleFactoryRegistry.getInstance().register(POINT, PointParticle.Factory::new);

        ClientPlayNetworking.registerGlobalReceiver(Weightless.FLYING_SOUND_S2C_PACKET, (client, handler, buf, sender) -> {
            if (client.world != null && client.player != null) {
                int id = buf.readVarInt();
                Entity entity = client.world.getEntityById(id);

                if (entity instanceof PlayerEntity player) {
                    WeightlessFlyingSoundInstance sound = new WeightlessFlyingSoundInstance(player, player == client.player);
                    FlyingPlayerTracker.startTrackingSound(client, player, sound);
                }
            }
        });
        ClientPlayNetworking.registerGlobalReceiver(Weightless.COMPARE_CONFIG_MATCH_S2C_PACKET, (client, handler, buf, sender) -> {
            int encoded = buf.readVarInt();
            if (encoded != ModConfig.encode()) {
                handler.getConnection().disconnect(DISCONNECT_MESSAGE);
            }
        });

        ClientTickEvents.START_CLIENT_TICK.register(client -> {
            if (TOGGLE_WEIGHTLESS.isPressed() && !wasWeightlessPressed) {
                weightlessActive = !weightlessActive;
                wasWeightlessPressed = true;
                TOGGLE_WEIGHTLESS.setPressed(false);
            }
            else if (wasWeightlessPressed) {
                wasWeightlessPressed = false;
            }

            if (AUTOPILOT.isPressed() && !wasAutopilotPressed) {
                autopilotActive = !autopilotActive;
                wasAutopilotPressed = true;
                AUTOPILOT.setPressed(false);
            }
            else if (wasAutopilotPressed) {
                wasAutopilotPressed = false;
            }
            WeightlessComponent.clientTick(client);
        });

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player != null) {
                boolean bl = client.player.isSprinting() || autopilotActive;
                flying = WeightlessComponent.flying(client.player) && bl;
                flySpeed = (float) Math.min(client.player.getVelocity().lengthSquared(), 1.0f);
            }
            if (client.world != null) {
                worldTime = client.world.getTime();
                FlyingPlayerTracker.update(client);
            }
        });

        ShaderEffectRenderCallback.EVENT.register(tickDelta -> {
            if (flying && ModConfig.renderSpeedlines) {
                WORLD_TIME.set(worldTime + tickDelta);
                FLY_SPEED.set(flySpeed);
                SPEED_LINES.render(tickDelta);
            }
        });

        WorldRenderEvents.AFTER_TRANSLUCENT.register(ctx -> {
            if (ModConfig.renderTrail) {
                MinecraftClient client = MinecraftClient.getInstance();
                if (client.world != null) {
                    float tickDelta = client.getTickDelta();
                    MatrixStack matrices = ctx.matrixStack();
                    VertexConsumerProvider vertexConsumers = ctx.consumers();

                    for (AbstractClientPlayerEntity player : client.world.getPlayers()) {
                        boolean bl = MinecraftClient.getInstance().options.getPerspective().isFirstPerson();
                        if (player != MinecraftClient.getInstance().player || !bl) {
                            if (!player.isSneaking() && WeightlessComponent.flying(player)) {
                                Vec3d velocity = player == MinecraftClient.getInstance().player ? player.lerpVelocity(tickDelta) : FlyingPlayerTracker.getLerpedVelocity(player, tickDelta);
                                double d = velocity.lengthSquared();
                                if (d > 1.0e-7) {
                                    Trail trail = WeightlessComponent.get(player).getTrail();
                                    Color color = WeightlessComponent.get(player).getTrailColor();

                                    float alpha = (float) Math.min(d + 0.2f, 1.0f);
                                    float width = 0.8f + (float) d;
                                    TrailRenderer.render(player, matrices, vertexConsumers, trail, width, alpha);

                                    if (d > 0.02 && ModConfig.spawnFlyingParticles && Math.random() < (player.isSprinting() ? 0.02 : 0.01)) {
                                        velocity = velocity.normalize().multiply(-0.25, 0.25, -0.25);
                                        MinecraftClient.getInstance().particleManager.addParticle(new ColorParticleEffect(new Vector3i(color.getRed(), color.getGreen(), color.getBlue())),
                                                player.getParticleX(0.5),
                                                player.getRandomBodyY(),
                                                player.getParticleZ(0.5),
                                                velocity.x, velocity.y, velocity.z
                                        );
                                    }
                                }
                            }
                        }
                    }
                }
            }
        });
    }
}
