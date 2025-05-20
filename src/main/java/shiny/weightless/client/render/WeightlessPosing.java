package shiny.weightless.client.render;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import shiny.weightless.common.component.WeightlessComponent;
import shiny.weightless.FlyingPlayerTracker;

public class WeightlessPosing {

    public static void updateTransforms(MatrixStack matrices, PlayerEntity player, float tickDelta) {
        //Y value for hovering
        float y = MathHelper.sin((player.age + tickDelta) * 0.05f) * 0.15f;
        matrices.translate(0.0f, y, 0.0f);
    }

    public static void setAngles(PlayerEntityModel<?> model, AbstractClientPlayerEntity player, float tickDelta) {
        Vec3d velocity;
        if (player == MinecraftClient.getInstance().player) {
            velocity = player.lerpVelocity(tickDelta);
        }
        else {
            velocity = FlyingPlayerTracker.getLerpedVelocity(player, tickDelta);
        }

        boolean bl = player.isSprinting() || WeightlessComponent.autopilot(player);
        Vec3d movement = WeightlessComponent.relativeMovement(velocity, player.getYaw(tickDelta), bl);

        double x = movement.x;
        double y = movement.y;
        double z = movement.z + Math.abs(x) * 0.5;

        double bound = Math.PI / 2.5;
        // Apply rotations

        //Lean the limbs by how much the player is moving forwards/backwards
        model.leftArm.pitch += (float) z;
        model.rightArm.pitch += (float) z;
        model.leftLeg.pitch += (float) z;
        model.rightLeg.pitch += (float) z;

        model.body.pitch += (float) (z * 0.5 + Math.abs(y * 0.001));

        //Sway the limbs by how much the player is strafing to the side and by how much the player is falling
        float left = (float) MathHelper.clamp(x * 1.2, -0.15, bound);
        float right = (float) MathHelper.clamp(x * 1.2, -bound, 0.15);
        model.leftArm.yaw += left;
        model.rightArm.yaw += right;
        model.leftLeg.yaw += left * 0.85f;
        model.rightLeg.yaw += right * 0.85f;
        model.body.yaw += (float) x * 0.2f;

        //Rotate the limbs by how much the player is falling
        float roll = (float) Math.min(0.0, y);
        model.leftArm.roll += roll;
        model.rightArm.roll -= roll;
        model.leftLeg.roll += roll;
        model.rightLeg.roll -= roll;

        //Adjust leg pivots based on new body rotations
        model.leftLeg.pivotY = 12.0f - Math.abs(model.body.pitch) * 4.0f;
        model.leftLeg.pivotZ = model.body.pitch * 10.0f;
        model.rightLeg.pivotY = 12.0f - Math.abs(model.body.pitch) * 4.0f;
        model.rightLeg.pivotZ = model.body.pitch * 10.0f;

        //Random leg oscillation
        model.leftLeg.roll -= MathHelper.sin(player.age * 0.09f) * 0.05f + 0.05f;
        model.rightLeg.roll += MathHelper.sin(player.age * 0.09f) * 0.05f + 0.05f;

        //Copy rotations to outer skin layers
        model.leftSleeve.copyTransform(model.leftArm);
        model.rightSleeve.copyTransform(model.rightArm);
        model.leftPants.copyTransform(model.leftLeg);
        model.rightPants.copyTransform(model.rightLeg);
        model.jacket.copyTransform(model.body);
    }
}
