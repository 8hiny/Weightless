package shiny.weightless;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.particle.ParticleType;
import net.minecraft.particle.SimpleParticleType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.text.Text;
import org.ladysnake.satin.api.event.ShaderEffectRenderCallback;
import org.ladysnake.satin.api.managed.ManagedShaderEffect;
import org.ladysnake.satin.api.managed.ShaderEffectManager;
import org.ladysnake.satin.api.managed.uniform.Uniform1f;
import shiny.weightless.client.particle.ColorParticleEffect;
import shiny.weightless.client.particle.PointParticle;
import shiny.weightless.client.particle.ShockwaveParticle;
import shiny.weightless.common.component.WeightlessComponent;
import shiny.weightless.common.network.CompareConfigMatchPayload;
import shiny.weightless.common.network.FlyingSoundPayload;

public class WeightlessClient implements ClientModInitializer {

    //Particles
    public static final SimpleParticleType SHOCKWAVE = Registry.register(Registries.PARTICLE_TYPE, Weightless.id("shockwave"), FabricParticleTypes.simple());
    public static final ParticleType<ColorParticleEffect> POINT = Registry.register(
            Registries.PARTICLE_TYPE,
            Weightless.id("point"),
            FabricParticleTypes.complex(ColorParticleEffect.CODEC, ColorParticleEffect.PACKET_CODEC)
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

        ClientPlayNetworking.registerGlobalReceiver(FlyingSoundPayload.ID, new FlyingSoundPayload.Handler());
        ClientPlayNetworking.registerGlobalReceiver(CompareConfigMatchPayload.ID, new CompareConfigMatchPayload.Handler());

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
    }
}
