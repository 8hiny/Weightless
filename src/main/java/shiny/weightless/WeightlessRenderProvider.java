package shiny.weightless;

import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public interface WeightlessRenderProvider {

    void setTickDelta(float tickDelta);
    float getTickDelta();

    void setLastLimbAngle(float limbAngle);
    float getLastLimbAngle();

    void setLastMovement(Vec3d movement);
    Vec3d getLastMovement();

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
