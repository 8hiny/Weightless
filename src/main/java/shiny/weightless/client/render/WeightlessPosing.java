package shiny.weightless.client.render;

import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import shiny.weightless.WeightlessRenderProvider;

public class WeightlessPosing {

    //Get the angles by which limbs should be moved based on the velocity of the player

    public static void updateTransforms(MatrixStack matrices, PlayerEntity player, float tickDelta) {
        //Y value for hovering
        float y = MathHelper.sin((player.age + tickDelta) * 0.05f) * 0.15f;
        matrices.translate(0.0f, y, 0.0f);
    }

    public static <T extends LivingEntity> void setAngles(PlayerEntityModel<T> model, PlayerEntity player, float animationProgress) {
        if (player instanceof WeightlessRenderProvider provider) {
            float tickDelta = provider.getTickDelta();

            //Interpolate with last values
            Vec3d lastMovement = provider.getLastMovement();
            Vec3d velocity = new Vec3d(player.getX() - player.prevX, player.getY() - player.prevY, player.getZ() - player.prevZ);
            Vec3d movement = WeightlessRenderProvider.relativeMovement(velocity, player.getYaw(), player.isSprinting());
            provider.setLastMovement(movement);
            movement = movement.lerp(lastMovement, tickDelta);

            double x = movement.x;
            double y = movement.y;
            double z = movement.z;

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
            model.leftLeg.roll -= MathHelper.sin(animationProgress * 0.09f) * 0.05f + 0.05f;
            model.rightLeg.roll += MathHelper.sin(animationProgress * 0.09f) * 0.05f + 0.05f;

            //Copy rotations to outer skin layers
            model.leftSleeve.copyTransform(model.leftArm);
            model.rightSleeve.copyTransform(model.rightArm);
            model.leftPants.copyTransform(model.leftLeg);
            model.rightPants.copyTransform(model.rightLeg);
            model.jacket.copyTransform(model.body);
        }
    }

    public static void oscillate(ModelPart limb, float random, float last) {
        //Use this to apply random motion to a modelpart, want to sorta mimic the oscillation from project jjk's first person animations
    }
}
