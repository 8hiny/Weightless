package shiny.weightless.common.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Tuple;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import shiny.weightless.client.sound.WeightlessFlyingSoundInstance;
import shiny.weightless.common.component.WeightlessComponent;
import shiny.weightless.common.config.ModConfig;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class FlyingPlayerTracker {

    /// Player velocities tracked by the client. The first vector is the old velocity, the second one is the new velocity.
    private static final Map<UUID, Tuple<Vec3, Vec3>> trackedVelocities = new HashMap<>();
    private static final Map<UUID, WeightlessFlyingSoundInstance> trackedSounds = new HashMap<>();

    public static void update(Minecraft client) {
        updateTrackedVelocity(client);
        updateTrackedSounds(client);
    }

    private static void updateTrackedVelocity(Minecraft client) {
        for (Player player : client.level.players()) {
            updateVelocity(player, player instanceof LocalPlayer);
        }
    }

    private static void updateVelocity(Player player, boolean local) {
        Tuple<Vec3, Vec3> velocities = trackedVelocities.get(player.getUUID());
        if (velocities == null) {
            startTrackingVelocity(player, local);
        }
        else {
            velocities.setA(velocities.getB());
            velocities.setB(
                    velocities.getA().scale(0.8).add(getPlayerVelocity(player, local).scale(0.2))
            );
        }
    }

    private static void startTrackingVelocity(Player player, boolean local) {
        trackedVelocities.putIfAbsent(player.getUUID(), new Tuple<>(Vec3.ZERO, getPlayerVelocity(player, local)));
    }

    private static Vec3 getPlayerVelocity(Player player, boolean local) {
        return local ? player.getDeltaMovement() : new Vec3(player.getX() - player.xOld, player.getY() - player.yOld, player.getZ() - player.zOld);
    }

    public static boolean isTrackingVelocity(Player player) {
        return trackedVelocities.containsKey(player.getUUID());
    }

    public static Vec3 getVelocity(UUID uuid) {
        Tuple<Vec3, Vec3> velocities = trackedVelocities.get(uuid);
        if (velocities != null) {
            return velocities.getB();
        }
        return Vec3.ZERO;
    }

    public static Vec3 getLerpedVelocity(UUID uuid, float tickDelta) {
        Tuple<Vec3, Vec3> velocities = trackedVelocities.get(uuid);
        if (velocities != null) {
            return velocities.getA().lerp(velocities.getB(), tickDelta);
        }
        return Vec3.ZERO;
    }

    public static boolean isTrackingSound(UUID uuid) {
        return trackedSounds.containsKey(uuid);
    }

    private static void updateTrackedSounds(Minecraft client) {
        for (Player player : client.level.players()) {
            if (!isTrackingSound(player.getUUID()) && WeightlessComponent.flying(player) && player.isSprinting()) {
                boolean bl = player instanceof LocalPlayer;
                if (!bl || ModConfig.selfFlightSound) {
                    WeightlessFlyingSoundInstance sound = new WeightlessFlyingSoundInstance(player, bl);
                    startTrackingSound(player, sound);
                }
            }
        }
    }

    public static void startTrackingSound(Player source, WeightlessFlyingSoundInstance sound) {
        stopExistingSound(source);
        trackedSounds.putIfAbsent(source.getUUID(), sound);
        Minecraft.getInstance().getSoundManager().play(sound);
    }

    private static void stopExistingSound(Player source) {
        UUID uuid = source.getUUID();
        if (trackedSounds.containsKey(uuid)) {
            Minecraft.getInstance().getSoundManager().stop(trackedSounds.get(uuid));
            removeSound(uuid);
        }
    }

    public static void removeSound(UUID uuid) {
        trackedSounds.remove(uuid);
    }
}
