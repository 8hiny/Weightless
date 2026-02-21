package shiny.weightless.client.util;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.AnimationUtils;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.phys.Vec3;
import shiny.weightless.client.render.RenderStateDataKeys;
import shiny.weightless.common.config.ModConfig;
import shiny.weightless.common.util.WeightlessUtil;

public class WeightlessPosing {

    public static void updateTransforms(PoseStack matrices, int flightTime, float tickDelta) {
        float y = -Mth.sin((flightTime + tickDelta) * 0.04f) * 0.08f;
        matrices.translate(0.0f, y, 0.0f);
    }

    public static <T extends HumanoidModel<?>> void setAngles(T model, HumanoidRenderState state) {
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
            model.leftArm.zRot -= x * 0.5f + z * 0.2f;
        }

        if (state.rightHandItemStack.isEmpty()) {
            model.rightArm.xRot += z;
            model.rightArm.yRot -= x;
            model.rightArm.zRot -= x * 0.5f - z * 0.2f;
        }

        model.leftLeg.xRot += z - Math.min(x * 0.8f, 0.0f);
        model.leftLeg.zRot = model.leftLeg.zRot - z * 0.15f + x * (x > 0 ? 1.5f : 2.5f);

        model.rightLeg.xRot += z + Math.max(x * 0.8f, 0.0f);
        model.rightLeg.zRot = model.rightLeg.zRot + z * 0.15f + x * (x > 0 ? 2.5f : 1.5f);

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
        if (y < 0.0f && pitch > -0.1f) {
            pitch = Math.min((float) Math.PI, pitch * (float) (Math.PI / 180) * -y * 2.2f);
            model.body.xRot += pitch * 0.9f;

            model.leftArm.xRot += pitch;
            model.leftArm.yRot += pitch * 0.1f;
            model.leftArm.zRot += pitch * 0.05f;

            model.rightArm.xRot += pitch;
            model.rightArm.yRot -= pitch * 0.1f;
            model.rightArm.zRot -= pitch * 0.05f;

            model.leftLeg.xRot += pitch;
            model.leftLeg.yRot += pitch * 0.05f;
            model.leftLeg.zRot += pitch * 0.02f;

            model.rightLeg.xRot += pitch;
            model.rightLeg.yRot -= pitch * 0.05f;
            model.rightLeg.zRot += pitch * 0.02f;
        }

        //Limit limb rotations to prevent clipping
        model.leftArm.yRot = Math.max(model.leftArm.yRot, model.body.yRot);
        model.rightArm.yRot = Math.min(model.rightArm.yRot, model.body.yRot);

        if (model.body.zRot < model.leftArm.zRot) {
            model.leftArm.zRot = model.body.zRot;
        }
        if (model.body.zRot > model.rightArm.zRot) {
            model.rightArm.zRot = model.body.zRot;
        }

        if (y > -0.2f) {
            model.leftLeg.zRot = Math.min(model.leftLeg.zRot, model.rightLeg.zRot);
            model.rightLeg.zRot = Math.max(model.rightLeg.zRot, model.leftLeg.zRot);
        }

        //Oscillate legs
        AnimationUtils.bobModelPart(model.leftLeg, state.ageInTicks, -0.2f);
        AnimationUtils.bobModelPart(model.rightLeg, state.ageInTicks, 0.2f);

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
