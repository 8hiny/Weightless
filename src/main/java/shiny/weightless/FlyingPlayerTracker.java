package shiny.weightless;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Pair;
import net.minecraft.util.math.Vec3d;
import shiny.weightless.client.sound.WeightlessFlyingSoundInstance;
import shiny.weightless.common.component.WeightlessComponent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class FlyingPlayerTracker {

    private static final Map<UUID, Pair<Vec3d, Vec3d>> trackedVelocities = new HashMap<>();
    private static final Map<UUID, WeightlessFlyingSoundInstance> trackedSounds = new HashMap<>();

    public static void update(MinecraftClient client) {
        updateTrackedVelocity(client);
        updateTrackedSounds(client);
    }

    public static void updateTrackedVelocity(MinecraftClient client) {
        for (PlayerEntity player : client.world.getPlayers()) {
            if (player != client.player) {
                if (!trackingVelocity(player)) startTrackingVelocity(player);
                else updateVelocity(player);
            }
        }
    }

    public static boolean trackingVelocity(PlayerEntity player) {
        return trackedVelocities.containsKey(player.getUuid());
    }

    public static void startTrackingVelocity(PlayerEntity player) {
        Vec3d velocity = new Vec3d(player.getX() - player.prevX, player.getY() - player.prevY, player.getZ() - player.prevZ);
        trackedVelocities.putIfAbsent(player.getUuid(), new Pair<>(Vec3d.ZERO, velocity));
    }

    public static void updateVelocity(PlayerEntity player) {
        Pair<Vec3d, Vec3d> velocities = trackedVelocities.get(player.getUuid());
        if (velocities != null) {
            Vec3d velocity = new Vec3d(player.getX() - player.prevX, player.getY() - player.prevY, player.getZ() - player.prevZ);
            velocities.setLeft(velocities.getRight());

            velocity = velocities.getLeft().multiply(0.8).add(velocity.multiply(0.2));
            velocities.setRight(velocity);
        }
    }

    public static Vec3d getVelocity(PlayerEntity player) {
        Pair<Vec3d, Vec3d> velocities = trackedVelocities.get(player.getUuid());
        if (velocities != null) {
            return velocities.getRight();
        }
        return Vec3d.ZERO;
    }

    public static Vec3d getLerpedVelocity(PlayerEntity player, float tickDelta) {
        Pair<Vec3d, Vec3d> velocities = trackedVelocities.get(player.getUuid());
        if (velocities != null) {
            return velocities.getLeft().lerp(velocities.getRight(), tickDelta);
        }
        return Vec3d.ZERO;
    }

    public static void updateTrackedSounds(MinecraftClient client) {
        for (PlayerEntity player : client.world.getPlayers()) {
            if (player != client.player) {
                boolean bl = WeightlessComponent.flying(player) && (player.isSprinting() || WeightlessComponent.autopilot(player));
                if (!trackingSound(player) && bl) {
                    WeightlessFlyingSoundInstance sound = new WeightlessFlyingSoundInstance(player, player == client.player);
                    startTrackingSound(client, player, sound);
                }
            }
        }
    }

    public static boolean trackingSound(PlayerEntity player) {
        return trackedSounds.containsKey(player.getUuid());
    }

    public static void startTrackingSound(MinecraftClient client, PlayerEntity source, WeightlessFlyingSoundInstance sound) {
        stopExistingSound(source);
        trackedSounds.putIfAbsent(source.getUuid(), sound);
        client.getSoundManager().play(sound);
    }

    public static void stopExistingSound(PlayerEntity source) {
        UUID uuid = source.getUuid();
        if (trackedSounds.containsKey(uuid)) {
            MinecraftClient client = MinecraftClient.getInstance();
            client.getSoundManager().stop(trackedSounds.get(uuid));
            remove(uuid);
        }
    }

    public static void remove(UUID uuid) {
        trackedSounds.remove(uuid);
    }
}
