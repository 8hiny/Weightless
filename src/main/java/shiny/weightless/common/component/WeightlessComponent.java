package shiny.weightless.common.component;

import dev.onyxstudios.cca.api.v3.component.sync.AutoSyncedComponent;
import dev.onyxstudios.cca.api.v3.component.tick.CommonTickingComponent;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;
import shiny.weightless.ModComponents;
import shiny.weightless.ModConfig;
import shiny.weightless.Weightless;
import shiny.weightless.WeightlessClient;
import shiny.weightless.client.trail.Trail;

import java.awt.*;
import java.util.ArrayList;

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

    public static void clientTick(MinecraftClient client) {
        ModComponents.WEIGHTLESS.maybeGet(client.player).ifPresent(component -> {
            boolean toggled = WeightlessClient.weightlessActive;
            boolean autopilot = WeightlessClient.autopilotActive;

            if (component.toggled != toggled) {
                PacketByteBuf buf = PacketByteBufs.create();
                buf.writeBoolean(toggled);
                ClientPlayNetworking.send(Weightless.WEIGHTLESS_TOGGLE_C2S_PACKET, buf);
                component.toggled = toggled;
            }
            if (component.autopilot != autopilot) {
                PacketByteBuf buf = PacketByteBufs.create();
                buf.writeBoolean(autopilot);
                ClientPlayNetworking.send(Weightless.AUTOPILOT_TOGGLE_C2S_PACKET, buf);
                component.autopilot = autopilot;
            }

            if (!client.isPaused()) {
                int red = ModConfig.trailRed;
                int green = ModConfig.trailGreen;
                int blue = ModConfig.trailBlue;
                if (component.trailRed != red || component.trailGreen != green || component.trailBlue != blue) {
                    PacketByteBuf buf = PacketByteBufs.create();
                    buf.writeInt(red);
                    buf.writeInt(green);
                    buf.writeInt(blue);
                    ClientPlayNetworking.send(Weightless.UPDATE_TRAIL_COLOR_C2S_PACKET, buf);

                    component.trailRed = red;
                    component.trailGreen = green;
                    component.trailBlue = blue;
                }
            }
        });
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

    public boolean autopilot() {
        return this.autopilot && this.provider.getHungerManager().getFoodLevel() > 6.0f && canFly(this.provider);
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
    public void readFromNbt(NbtCompound tag) {
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
    public void writeToNbt(NbtCompound tag) {
        tag.putBoolean("Enabled", this.enabled);
        tag.putBoolean("Toggled", this.toggled);
        tag.putBoolean("Autopilot", this.autopilot);
        tag.putBoolean("Flying", this.flying);
        tag.putInt("StunTicks", this.remainingStunTicks);
        tag.putInt("TrailRed", this.trailRed);
        tag.putInt("TrailGreen", this.trailGreen);
        tag.putInt("TrailBlue", this.trailBlue);
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

    public void setToggled(boolean toggled) {
        this.toggled = toggled;
        sync();
    }

    public void setAutopilot(boolean autopilot) {
        this.autopilot = autopilot;
        sync();
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

    public static Vec3d relativeMovement(Vec3d velocity, float yaw, boolean sprinting) {
        Vec3d movement = velocity.rotateY(yaw * (float) Math.PI / 180);
        double x = movement.x;
        double y = velocity.y;
        double z = movement.z;

        if (sprinting) {
            x *= 1.1f;
            z *= 1.25f;
        }

        double bound = Math.PI / 2.5;
        if (y < 0.0f) {
            x *= 1.0f - y;
            z = MathHelper.lerp(y, z, -bound);
        }

        x = MathHelper.clamp(x, -bound, bound);
        y = MathHelper.clamp(y, -bound, bound);
        z = MathHelper.clamp(z, -bound, bound);

        return new Vec3d(x, y, z);
    }
}
