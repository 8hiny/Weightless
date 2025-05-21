package shiny.weightless.common.component;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;
import org.ladysnake.cca.api.v3.component.sync.AutoSyncedComponent;
import org.ladysnake.cca.api.v3.component.tick.CommonTickingComponent;
import shiny.weightless.ModComponents;
import shiny.weightless.ModConfig;
import shiny.weightless.WeightlessClient;
import shiny.weightless.client.trail.Trail;
import shiny.weightless.common.network.ToggleAutopilotPayload;
import shiny.weightless.common.network.ToggleWeightlessPayload;
import shiny.weightless.common.network.UpdateTrailColorPayload;

import java.awt.*;

public class WeightlessComponent implements AutoSyncedComponent, CommonTickingComponent {

    private final PlayerEntity provider;
    private final Trail trail = new Trail(20);
    private int remainingStunTicks;
    private boolean enabled;
    private boolean flying;
    private boolean toggled = true;
    private boolean autopilot = false;
    private int trailRed = 255;
    private int trailGreen = 255;
    private int trailBlue = 255;

    public WeightlessComponent(PlayerEntity provider) {
        this.provider = provider;
    }

    public static WeightlessComponent get(@NotNull PlayerEntity player) {
        return ModComponents.WEIGHTLESS.get(player);
    }

    public static boolean has(@NotNull PlayerEntity player) {
        return ModComponents.WEIGHTLESS.get(player).has();
    }

    public static boolean flying(@NotNull PlayerEntity player) {
        return ModComponents.WEIGHTLESS.get(player).flying();
    }

    public static boolean autopilot(@NotNull PlayerEntity player) {
        return ModComponents.WEIGHTLESS.get(player).autopilot();
    }

    public void sync() {
        ModComponents.WEIGHTLESS.sync(this.provider);
    }

    @Override
    public void tick() {
        if (this.remainingStunTicks > 0) this.remainingStunTicks--;
    }

    @Override
    public void clientTick() {
        tick();

        Vec3d pos = new Vec3d(this.provider.getX(), this.provider.getY() + 2.0, this.provider.getZ());
        this.trail.addPoint(pos);
        this.trail.tick();
    }

    public static void clientTick(MinecraftClient client) {
        ModComponents.WEIGHTLESS.maybeGet(client.player).ifPresent(component -> {
            boolean toggled = WeightlessClient.weightlessActive;
            boolean autopilot = WeightlessClient.autopilotActive;

            if (component.toggled != toggled) {
                ClientPlayNetworking.send(new ToggleWeightlessPayload());
                component.toggled = toggled;
            }
            if (component.autopilot != autopilot) {
                ClientPlayNetworking.send(new ToggleAutopilotPayload());
                component.autopilot = autopilot;
            }

            if (!client.isPaused()) {
                int red = ModConfig.trailRed;
                int green = ModConfig.trailGreen;
                int blue = ModConfig.trailBlue;

                if (component.trailRed != red || component.trailGreen != green || component.trailBlue != blue) {
                    ClientPlayNetworking.send(new UpdateTrailColorPayload(new Vector3f(red, green, blue)));
                    component.trailRed = red;
                    component.trailGreen = green;
                    component.trailBlue = blue;
                }
            }
        });
    }

    public boolean autopilot() {
        return this.autopilot && this.provider.getHungerManager().getFoodLevel() > 6.0f && canFly(this.provider);
    }

    public boolean toggled() {
        return this.toggled;
    }

    public boolean has() {
        return this.enabled;
    }

    public void attain() {
        this.enabled = true;
        sync();
    }

    public void reset() {
        this.enabled = false;
        this.toggled = true;
        this.autopilot = false;
        this.flying = false;
        this.remainingStunTicks = 0;
        sync();
    }

    @Override
    public void readFromNbt(NbtCompound tag, RegistryWrapper.WrapperLookup wrapperLookup) {
        this.enabled = tag.getBoolean("Enabled");
        this.toggled = tag.getBoolean("Toggled");
        this.autopilot = tag.getBoolean("Autopilot");
        this.flying = tag.getBoolean("Flying");
        this.remainingStunTicks = tag.getInt("StunTicks");
        this.trailRed = tag.getInt("TrailRed");
        this.trailGreen = tag.getInt("TrailGreen");
        this.trailBlue = tag.getInt("TrailBlue");
    }

    @Override
    public void writeToNbt(NbtCompound tag, RegistryWrapper.WrapperLookup wrapperLookup) {
        tag.putBoolean("Enabled", this.enabled);
        tag.putBoolean("Toggled", this.toggled);
        tag.putBoolean("Autopilot", this.autopilot);
        tag.putBoolean("Flying", this.flying);
        tag.putInt("StunTicks", this.remainingStunTicks);
        tag.putInt("TrailRed", this.trailRed);
        tag.putInt("TrailGreen", this.trailGreen);
        tag.putInt("TrailBlue", this.trailBlue);
    }

    public void setToggled(boolean toggled) {
        this.toggled = toggled;
    }

    public void setAutopilot(boolean autopilot) {
        this.autopilot = autopilot;
    }

    public boolean isStunned() {
        return this.remainingStunTicks > 0;
    }

    public void setStunned() {
        this.remainingStunTicks = ModConfig.stunDuration;
        sync();
    }

    public boolean flying() {
        return this.has() && this.toggled && this.flying && canFly(this.provider);
    }

    public void setFlying(boolean flying) {
        if (flying != this.flying) {
            this.flying = flying;
            sync();
        }
    }

    public Trail getTrail() {
        return this.trail;
    }

    public void setTrailColor(int red, int green, int blue) {
        this.trailRed = red;
        this.trailGreen = green;
        this.trailBlue = blue;
        sync();
    }

    public Color getTrailColor() {
        return new Color(this.trailRed, this.trailGreen, this.trailBlue);
    }

    public static boolean canFly(PlayerEntity player) {
        return player.isPartOfGame()
                && !player.isCreative()
                && !player.hasVehicle()
                && !player.isInSwimmingPose()
                && !player.isUsingRiptide()
                && !player.isFallFlying()
                && !player.isSleeping()
                && !player.isClimbing();
    }
}
