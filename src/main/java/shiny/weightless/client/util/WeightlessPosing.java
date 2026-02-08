package shiny.weightless.client.util;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.phys.Vec3;
import shiny.weightless.common.config.ModConfig;
import shiny.weightless.common.util.WeightlessUtil;

public class WeightlessPosing {

    public static void updateTransforms(PoseStack matrices, int flightTime, float tickDelta) {
        float y = -Mth.sin((flightTime + tickDelta) * 0.04f) * 0.08f;
        matrices.translate(0.0f, y, 0.0f);
    }

    //TODO Clamp body and limb rotations to prevent clipping and weirdness at high speeds
    //TODO Apply arm oscillation to legs too (but opposite, so transforms for left arm apply to right leg)
    public static <T extends HumanoidModel<?>> void setAngles(T model, AvatarRenderState state) {
        Vec3 velocity = state.getDataOrDefault(RenderStateDataKeys.VELOCITY, Vec3.ZERO);
        Vec3 movement = WeightlessUtil.calcDirectionalMovement(velocity, state.bodyRot);
        float pitch = state.getDataOrDefault(RenderStateDataKeys.PITCH, 0.0f);

        float x = (float) movement.x;
        float y = (float) movement.y;
        float z = (float) movement.z + Math.abs(x) * 0.5f;
        boolean leftHanded = state.mainArm == HumanoidArm.LEFT;

        //Lean limbs in opposite direction to velocity
        model.body.xRot += z * 0.7f;
        model.body.yRot -= x - x * z * 1.5f;
        model.body.zRot += x;

        if (state.leftHandItemStack.isEmpty()) {
            model.leftArm.xRot += z;
            model.leftArm.yRot -= x;
            //model.leftArm.zRot = model.leftArm.zRot - z * 0.2f - x * 0.5f;
            model.leftArm.zRot -= x * 0.5f - z * 0.2f;
        }

        if (state.rightHandItemStack.isEmpty()) {
            model.rightArm.xRot += z;
            model.rightArm.yRot -= x;
            //model.rightArm.zRot = model.rightArm.zRot + z * 0.2f + x * 0.5f;
            model.rightArm.zRot -= x * 0.5f + z * 0.2f;
        }

        model.leftLeg.xRot += z - Math.min(x * 1.5f, 0.0f);
        model.leftLeg.zRot = model.leftLeg.zRot - z * 0.15f + x * (x > 0 ? 2.0f : 3.0f);

        model.rightLeg.xRot += z + Math.max(x * 1.5f, 0.0f);
        model.rightLeg.zRot = model.rightLeg.zRot + z * 0.15f + x * (x > 0 ? 3.0f : 2.0f);

        //Lift leg matching the main hand
        if (ModConfig.legLiftedPose) {
            if (leftHanded) {
                model.leftLeg.xRot *= 0.f;
                model.leftLeg.y -= 2.0f;
                model.leftLeg.z -= 2.0f + z * 0.5f;
            } else {
                model.rightLeg.xRot *= 0.8f;
                model.rightLeg.y -= 2.0f;
                model.rightLeg.z -= 2.0f + z * 0.5f;
            }
        }

        //Spread out limbs when flying downwards
        if (y < 0.0f && pitch > 0.0f) {
            pitch = Math.min((float) Math.PI, pitch * (float) (Math.PI / 180) * -y * 2.2f);
            model.body.xRot += pitch * 0.9f;

            model.leftArm.xRot += pitch;
            model.rightArm.xRot += pitch;

            model.leftLeg.xRot += pitch;
            model.rightLeg.xRot += pitch;
        }

        //Limit limb rotations to prevent clipping
        model.leftArm.zRot = Math.min(model.leftArm.zRot, model.body.zRot);
        model.rightArm.zRot = Math.max(model.rightArm.zRot, model.body.zRot);

        model.leftLeg.zRot = Math.min(model.leftLeg.zRot, model.rightLeg.zRot);
        model.rightLeg.zRot = Math.max(model.rightLeg.zRot, model.leftLeg.zRot);

        //Reposition pivot points
        rotatePivotAround(model.leftArm, model.body);
        rotatePivotAround(model.rightArm, model.body);
        rotatePivotAround(model.leftLeg, model.body);
        rotatePivotAround(model.rightLeg, model.body);
    }

    private static void rotatePivotAround(ModelPart affected, ModelPart rotator) {
        Vec3 pivot = new Vec3(affected.x, affected.y, affected.z)
                .xRot(-rotator.xRot)
                .yRot(rotator.yRot)
                .zRot(-rotator.zRot);

        affected.x = (float) pivot.x;
        affected.y = (float) pivot.y;
        affected.z = (float) pivot.z;
    }
}
