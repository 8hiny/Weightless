package shiny.weightless;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Pair;
import net.minecraft.util.math.Vec3d;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class VelocityTracker {

    private static final Map<UUID, Pair<Vec3d, Vec3d>> trackedVelocities = new HashMap<>();

    public static boolean tracking(PlayerEntity player) {
        return trackedVelocities.containsKey(player.getUuid());
    }

    public static void startTracking(PlayerEntity player) {
        Vec3d velocity = new Vec3d(player.getX() - player.prevX, player.getY() - player.prevY, player.getZ() - player.prevZ);
        trackedVelocities.putIfAbsent(player.getUuid(), new Pair<>(Vec3d.ZERO, velocity));
    }

    public static void update(MinecraftClient client) {
        if (client.world != null) {
            for (PlayerEntity player : client.world.getPlayers()) {
                if (player != client.player) {
                    if (!tracking(player)) startTracking(player);
                    else updateVelocity(player);
                }
            }
        }
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
}
