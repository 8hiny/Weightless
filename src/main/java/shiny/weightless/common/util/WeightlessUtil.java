package shiny.weightless.common.util;

import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockCollisions;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import shiny.weightless.common.config.ModConfig;
import shiny.weightless.common.component.WeightlessComponent;
import shiny.weightless.common.network.FlyingSoundPayload;

public class WeightlessUtil {

    public static boolean canFly(Player player) {
        return player.canBeSeenByAnyone()
                && !player.isCreative()
                && !player.isPassenger()
                && !player.isVisuallySwimming()
                && !player.isAutoSpinAttack()
                && !player.isFallFlying()
                && !player.isSleeping()
                && !player.onClimbable();
    }

    public static boolean canReceiveAltitudeBonus(Entity entity) {
        return ModConfig.increaseSpeedWhenHigh && entity.position().y >= ModConfig.altitude && !WeightlessUtil.isNearBlock(entity, 8);
    }

    public static boolean isNearBlock(Entity entity, int radius) {
        for (int i = -radius; i <= radius; i++) {
            for (int j = -radius; j <= radius; j++) {
                for (int k = -radius; k <= radius; k++) {

                    BlockPos pos = new BlockPos(entity.blockPosition().offset(i, j, k));
                    if (entity.level().getBlockState(pos).blocksMotion()) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public static boolean isCollidedWithBlock(Entity entity) {
        Level world = entity.level();
        AABB box = entity.getBoundingBox().expandTowards(0, -0.01, 0);
        BlockCollisions<VoxelShape> spliterator = new BlockCollisions<>(world, entity, box, false, (mutable, voxelShape) -> voxelShape);

        while (spliterator.hasNext()) {
            VoxelShape shape = spliterator.next();
            if (!shape.isEmpty()) {
                return true;
            }
        }
        return false;
    }

    public static float calcFlightSpeed(LivingEntity entity, boolean sprinting) {
        if (entity instanceof Player player && WeightlessComponent.get(player).isStunned()) {
            return 0.0f;
        }

        float speed = ModConfig.movementSpeedAffectSpeed ? entity.getSpeed() : 0.1f;
        speed *= sprinting ? 1.0f : 0.35f;
        speed *= ModConfig.speedMultiplier;

        if (ModConfig.armorAffectSpeed) {
            speed *= Math.max(0.1f, -0.025f * entity.getArmorValue() + 1);
        }

        if (canReceiveAltitudeBonus(entity)) {
            speed *= ModConfig.highSpeedMultiplier;
        }
        return speed;
    }

    public static Vec3 inputToFlightVelocity(Vec3 movementInput, float speed, float pitch, float yaw) {
        double d = movementInput.lengthSqr();
        if (d < 1.0e-7) {
            return Vec3.ZERO;
        }
        else {
            Vec3 vec3d = (d > 1.0 ? movementInput.normalize() : movementInput).scale(speed);
            float vertical = (float) Math.sqrt(1.0f - Math.abs(pitch) / 90.0f);
            float x = Mth.sin(yaw * (float) (Math.PI / 180.0)) * vertical;
            float y = -Mth.sin(pitch * (float) (Math.PI / 180.0));
            float z = Mth.cos(yaw * (float) (Math.PI / 180.0)) * vertical;

            return new Vec3(
                    vec3d.x * z - vec3d.z * x,
                    (vec3d.z > 0 ? y : -y) * speed * (movementInput.z != 0 ? 1.1 : 0),
                    vec3d.z * z + vec3d.x * x
            );
        }
    }

    public static Vec3 calcDirectionalMovement(Vec3 velocity, float yaw, boolean sprinting) {
        Vec3 movement = velocity.yRot(yaw * (float) Math.PI / 180);
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
            z = Mth.lerp(y, z, -bound);
        }

        x = Mth.clamp(x, -bound, bound);
        y = Mth.clamp(y, -bound, bound);
        z = Mth.clamp(z, -bound, bound);

        return new Vec3(x, y, z);
    }

    public static void sendFlightSoundPackets(Player source) {
        if (!source.level().isClientSide()) {
            FlyingSoundPayload payload = new FlyingSoundPayload(source.getId());

            for (ServerPlayer recipient : PlayerLookup.tracking(source)) {
                ServerPlayNetworking.send(recipient, payload);
            }
            ServerPlayNetworking.send((ServerPlayer) source, payload);
        }
    }
}
