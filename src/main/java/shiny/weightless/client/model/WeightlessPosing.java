package shiny.weightless.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import shiny.weightless.common.util.WeightlessUtil;

public class WeightlessPosing {

    public static void updateTransforms(PoseStack matrices, int flightTime, float tickDelta) {
        float y = -Mth.sin((flightTime + tickDelta) * 0.04f) * 0.08f;
        matrices.translate(0.0f, y, 0.0f);
    }

    public static <T extends HumanoidModel<?>> void setAngles(T model, AvatarRenderState state, float tickDelta) {
        boolean sprinting = Boolean.TRUE.equals(state.getData(RenderStateDataKeys.IS_SPRINTING));
        Vec3 velocity = state.getDataOrDefault(RenderStateDataKeys.VELOCITY, Vec3.ZERO);
        Vec3 movement = WeightlessUtil.calcDirectionalMovement(velocity, state.bodyRot, sprinting);

        double x = movement.x;
        double y = movement.y;
        double z = movement.z + Math.abs(x) * 0.5;

        double bound = Math.PI / 2.5;
        // Apply rotations

        //Lean the limbs by how much the player is moving forwards/backwards
        model.leftArm.xRot += (float) z;
        model.rightArm.xRot += (float) z;
        model.leftLeg.xRot += (float) z;
        model.rightLeg.xRot += (float) z;

        model.body.xRot += (float) (z * 0.5 + Math.abs(y * 0.001));

        //Sway the limbs by how much the player is strafing to the side and by how much the player is falling
        float left = (float) Mth.clamp(x * 1.2, -0.15, bound);
        float right = (float) Mth.clamp(x * 1.2, -bound, 0.15);
        model.leftArm.yRot += left;
        model.rightArm.yRot += right;
        model.leftLeg.yRot += left * 0.85f;
        model.rightLeg.yRot += right * 0.85f;
        model.body.yRot += (float) x * 0.2f;

        //Rotate the limbs by how much the player is falling
        float roll = (float) Math.min(0.0, y);
        model.leftArm.zRot += roll;
        model.rightArm.zRot -= roll;
        model.leftLeg.zRot += roll;
        model.rightLeg.zRot -= roll;

        //Adjust leg pivots based on new body rotations
        model.leftLeg.y = 12.0f - Math.abs(model.body.xRot) * 4.0f;
        model.leftLeg.z = model.body.xRot * 10.0f;
        model.rightLeg.y = 12.0f - Math.abs(model.body.xRot) * 4.0f;
        model.rightLeg.z = model.body.xRot * 10.0f;

        //Random leg oscillation
        //model.leftLeg.zRot -= Mth.sin(player.tickCount * 0.09f) * 0.05f + 0.05f;
        //model.rightLeg.zRot += Mth.sin(player.tickCount * 0.09f) * 0.05f + 0.05f;
    }
}
