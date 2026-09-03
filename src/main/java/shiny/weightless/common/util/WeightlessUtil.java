package shiny.weightless.common.util;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockCollisions;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import shiny.weightless.common.config.ModConfig;

public final class WeightlessUtil {

    private WeightlessUtil() {
    }

    public static boolean canFly(Player player) {
        return player.canBeSeenByAnyone()
                && !player.isCreative()
                && !player.isPassenger()
                && !player.isVisuallySwimming()
                && !player.isAutoSpinAttack()
                && !player.isFallFlying()
                && !player.isSleeping()
                && !player.onClimbable()
                && !player.isInWater()
                && player.getFluidHeight(FluidTags.LAVA) <= player.getFluidJumpThreshold();
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
        BlockCollisions<VoxelShape> spliterator = new BlockCollisions<>(
                world, entity, box, false, (mutable, voxelShape) -> voxelShape
        );

        while (spliterator.hasNext()) {
            VoxelShape shape = spliterator.next();
            if (!shape.isEmpty()) {
                return true;
            }
        }
        return false;
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
            float y = -Mth.sin(pitch * (float) (Math.PI / 180.0)) * speed;
            float z = Mth.cos(yaw * (float) (Math.PI / 180.0)) * vertical;

            return new Vec3(
                    vec3d.x * z - vec3d.z * x,
                    (vec3d.z > 0.0 ? y : -y) * (movementInput.z != 0.0 ? 1.0 : 0.0),
                    vec3d.z * z + vec3d.x * x
            );
        }
    }

    public static Vec3 calcDirectionalMovement(Vec3 velocity, float yaw) {
        double d = velocity.lengthSqr();
        if (d < 1.0e-7) {
            return Vec3.ZERO;
        }
        else {
            Vec3 movement = velocity.yRot(yaw * (float) Math.PI / 180);
            double x = movement.x;
            double y = velocity.y;
            double z = movement.z;

            x = Mth.clamp(x, -0.65, 0.65);
            y = Mth.clamp(y, -0.75, 0.75);
            z = Mth.clamp(z, -1.0, 1.0);
            return new Vec3(x, y, z);
        }
    }

    public static Vec3 clampNearZero(Vec3 velocity) {
        double x = velocity.x;
        double y = velocity.y;
        double z = velocity.z;

        if (Math.abs(x) < 0.003) {
            x = 0.0;
        }
        if (Math.abs(y) < 0.003) {
            y = 0.0;
        }
        if (Math.abs(z) < 0.003) {
            z = 0.0;
        }
        return new Vec3(x, y, z);
    }

    public static void affectForFlight(Player player, Vec3 movementInput) {
        if (player.canSimulateMovement()) {
            float speed = ModConfig.calcFlightSpeed(player, player.isSprinting());
            Vec3 movement = inputToFlightVelocity(movementInput, speed, player.getXRot(), player.getYRot());
            Vec3 velocity = clampNearZero(
                    player.getDeltaMovement().add(movement).multiply(0.85, 0.85, 0.85)
            );

            player.setDeltaMovement(velocity);
            player.move(MoverType.SELF, player.getDeltaMovement());
        }

        float f = (float) (player.getY() - player.yOld);
        if (f < 0.0f) {
            player.fallDistance = Math.abs(f) * 10.0f;
        }
        else {
            player.fallDistance = 0.0f;
        }
    }
}
