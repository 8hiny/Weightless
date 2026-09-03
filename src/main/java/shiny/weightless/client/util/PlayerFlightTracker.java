package shiny.weightless.client.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.util.Tuple;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import shiny.weightless.client.sound.WeightlessSoundInstance;
import shiny.weightless.common.component.WeightlessComponent;
import shiny.weightless.common.config.ModConfig;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class PlayerFlightTracker {

    //Player velocities tracked by the client. The first vector is the old velocity, the second one is the new velocity.
    private final Map<UUID, Tuple<Vec3, Vec3>> trackedVelocities = new HashMap<>();
    private final Map<UUID, SoundInstance> trackedSounds = new HashMap<>();
    private static PlayerFlightTracker instance;

    private PlayerFlightTracker() {
    }

    public static PlayerFlightTracker getInstance() {
        if (instance == null) instance = new PlayerFlightTracker();
        return instance;
    }

    public void update(Minecraft client) {
        this.updateTrackedVelocity(client);
        this.updateTrackedSounds(client);
    }

    private void updateTrackedVelocity(Minecraft client) {
        for (Player player : client.level.players()) {
            this.updateVelocity(player, player instanceof LocalPlayer);
        }
    }

    private void updateVelocity(Player player, boolean local) {
        Tuple<Vec3, Vec3> velocities = this.trackedVelocities.get(player.getUUID());
        if (velocities == null) {
            this.startTrackingVelocity(player, local);
        } else {
            velocities.setA(velocities.getB());
            velocities.setB(
                    velocities.getA().scale(0.8).add(getClientPlayerVelocity(player, local).scale(0.2))
            );
        }
    }

    private void updateTrackedSounds(Minecraft client) {
        for (Player player : client.level.players()) {
            if (!this.isTrackingSound(player.getUUID()) && WeightlessComponent.flying(player)) {
                double speed = this.getLerpedVelocity(player.getUUID(),
                        client.getDeltaTracker().getGameTimeDeltaPartialTick(true)).lengthSqr();

                if (speed > 0.13 || player.isSprinting()) {
                    boolean bl = player instanceof LocalPlayer;
                    if (!bl || ModConfig.selfFlightSound) {
                        WeightlessSoundInstance sound = new WeightlessSoundInstance(player, bl);
                        this.startTrackingSound(client, sound, player);
                    }
                }
            }
        }
    }

    private void startTrackingVelocity(Player player, boolean local) {
        this.trackedVelocities.putIfAbsent(player.getUUID(), new Tuple<>(Vec3.ZERO, getClientPlayerVelocity(player, local)));
    }

    public void startTrackingSound(Minecraft client, WeightlessSoundInstance sound, Player source) {
        UUID uuid = source.getUUID();
        if (this.trackedSounds.containsKey(uuid)) {
            client.getSoundManager().stop(this.trackedSounds.get(uuid));
            this.removeSound(uuid);
        }
        this.trackedSounds.putIfAbsent(uuid, sound);
        client.getSoundManager().play(sound);
    }

    public Vec3 getVelocity(UUID uuid) {
        Tuple<Vec3, Vec3> velocities = this.trackedVelocities.get(uuid);
        if (velocities != null) {
            return velocities.getB();
        }
        return Vec3.ZERO;
    }

    public Vec3 getLerpedVelocity(UUID uuid, float tickDelta) {
        Tuple<Vec3, Vec3> velocities = this.trackedVelocities.get(uuid);
        if (velocities != null) {
            return velocities.getA().lerp(velocities.getB(), tickDelta);
        }
        return Vec3.ZERO;
    }

    public boolean isTrackingSound(UUID uuid) {
        return this.trackedSounds.containsKey(uuid);
    }

    public void removeSound(UUID uuid) {
        this.trackedSounds.remove(uuid);
    }

    private static Vec3 getClientPlayerVelocity(Player player, boolean local) {
        if (local) return player.getDeltaMovement();
        return new Vec3(
                player.getX() - player.xOld,
                player.getY() - player.yOld,
                player.getZ() - player.zOld
        );
    }
}
