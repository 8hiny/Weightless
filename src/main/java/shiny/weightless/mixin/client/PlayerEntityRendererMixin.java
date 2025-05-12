package shiny.weightless.mixin.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import shiny.weightless.WeightlessRenderProvider;
import shiny.weightless.client.render.TrailRenderer;
import shiny.weightless.client.render.WeightlessPosing;
import shiny.weightless.common.component.WeightlessComponent;

@Mixin(PlayerEntityRenderer.class)
public abstract class PlayerEntityRendererMixin extends LivingEntityRenderer<AbstractClientPlayerEntity, PlayerEntityModel<AbstractClientPlayerEntity>> {

    public PlayerEntityRendererMixin(EntityRendererFactory.Context ctx, PlayerEntityModel<AbstractClientPlayerEntity> model, float shadowRadius) {
        super(ctx, model, shadowRadius);
    }

    @Inject(method = "render(Lnet/minecraft/client/network/AbstractClientPlayerEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V", at = @At(value = "HEAD"))
    private void weightless$captureTickDelta(AbstractClientPlayerEntity player, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider provider, int light, CallbackInfo ci) {
        if (player instanceof WeightlessRenderProvider provider1) {
            provider1.setTickDelta(tickDelta);
        }
    }

    @Inject(method = "render(Lnet/minecraft/client/network/AbstractClientPlayerEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V", at = @At(value = "HEAD"))
    private void weightless$onRender(AbstractClientPlayerEntity player, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider provider, int light, CallbackInfo ci) {
        if (WeightlessComponent.flying(player) && !player.isSneaking()) {
            matrices.push();
            if (player.isSprinting()) {
                //WIP, trail renderer currently looks like crap
                //TrailRenderer.render(MinecraftClient.getInstance(), matrices, provider, player, 1.5f, light);
            }
            WeightlessPosing.updateTransforms(matrices, player, tickDelta);
        }
    }

    @Inject(method = "render(Lnet/minecraft/client/network/AbstractClientPlayerEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V", at = @At(value = "TAIL"))
    private void weightless$afterRender(AbstractClientPlayerEntity player, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider provider, int light, CallbackInfo ci) {
        if (WeightlessComponent.flying(player) && !player.isSneaking()) {
            matrices.pop();
        }
    }

    //Old version of the renderer which mimicked elytra flight
//    @Inject(method = "setupTransforms(Lnet/minecraft/client/network/AbstractClientPlayerEntity;Lnet/minecraft/client/util/math/MatrixStack;FFF)V", at = @At(value = "HEAD"), cancellable = true)
//    private void weightless$setUpTransforms(AbstractClientPlayerEntity player, MatrixStack matrixStack, float animationProgress, float bodyYaw, float tickDelta, CallbackInfo ci) {
//        if (WeightlessComponent.flying(player) && !player.horizontalCollision && player.isSprinting()) {
//            super.setupTransforms(player, matrixStack, animationProgress, bodyYaw, tickDelta);
//
//            float j = (float) player.getRoll() + tickDelta;
//            float k = MathHelper.clamp(j * j / 100.0f, 0.0F, 1.0f);
//            matrixStack.multiply(RotationAxis.POSITIVE_X.rotationDegrees(k * (-90.0f - player.getPitch())));
//
//            Vec3d rotation = player.getRotationVec(tickDelta);
//            Vec3d velocity = player.lerpVelocity(tickDelta);
//            double d = velocity.horizontalLengthSquared();
//            double e = rotation.horizontalLengthSquared();
//
//            double strength = (Math.abs(player.getYaw() - player.prevYaw) + Math.abs(player.getPitch() - player.prevPitch)) * 0.08;
//
//            if (d > 0.0 && e > 0.0) {
//                double l = (velocity.x * rotation.x + velocity.z * rotation.z) / Math.sqrt(d * e);
//                double m = velocity.x * rotation.z - velocity.z * rotation.x;
//
//                if (MinecraftClient.getInstance().player != null && !MinecraftClient.getInstance().player.getUuid().equals(player.getUuid()) && player instanceof WeightlessRenderProvider) {
//                    m = player.getYaw() - player.prevYaw;
//                    l = MathHelper.clamp(1 - (0.8 * strength * strength), 0.4, 1.0);
//                }
//
//                float angle = (float) Math.acos(l);
//                if (player instanceof WeightlessRenderProvider provider) {
//                    provider.weightless$setLeanAngle(angle);
//
//                    if (provider.weightless$getLeanAngle() > 0.0) {
//                        angle = MathHelper.lerp(tickDelta * 1.25f, provider.weightless$getLeanAngle(), angle);
//                    }
//                }
//                float bound = (float) Math.PI / 4;
//                matrixStack.multiply(RotationAxis.POSITIVE_Y.rotation((float) (Math.signum(m) * MathHelper.clamp(angle, -bound, bound))));
//            }
//            ci.cancel();
//        }
//    }
}
