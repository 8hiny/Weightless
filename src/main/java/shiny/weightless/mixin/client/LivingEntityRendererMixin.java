package shiny.weightless.mixin.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.Vec3d;
import org.joml.Vector3i;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import shiny.weightless.FlyingPlayerTracker;
import shiny.weightless.ModConfig;
import shiny.weightless.client.particle.ColorParticleEffect;
import shiny.weightless.client.render.TrailRenderer;
import shiny.weightless.client.render.WeightlessPosing;
import shiny.weightless.client.trail.Trail;
import shiny.weightless.common.component.WeightlessComponent;

import java.awt.*;

@Mixin(LivingEntityRenderer.class)
public class LivingEntityRendererMixin<T extends LivingEntity, M extends EntityModel<T>> {

    @Shadow protected M model;

    @Inject(method = "render(Lnet/minecraft/entity/LivingEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/entity/LivingEntityRenderer;setupTransforms(Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/client/util/math/MatrixStack;FFF)V"))
    private void weightless$renderTrail(T entity, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumerProvider, int light, CallbackInfo ci) {
        if (entity instanceof AbstractClientPlayerEntity player && !player.isSneaking() && WeightlessComponent.flying(player) && ModConfig.renderTrail) {
            Vec3d velocity = player == MinecraftClient.getInstance().player ? player.lerpVelocity(tickDelta) : FlyingPlayerTracker.getLerpedVelocity(player, tickDelta);
            double d = velocity.lengthSquared();
            if (d > 1.0E-7) {
                Trail trail = WeightlessComponent.get(player).getTrail();
                Color color = WeightlessComponent.get(player).getTrailColor();

                int alpha = (int) (255 * Math.min(d + 0.2f, 1.0f));
                float size = 0.8f + (float) d;
                TrailRenderer.render(MinecraftClient.getInstance(), matrices, vertexConsumerProvider, trail, color, size, alpha);

                if (d > 0.02 && ModConfig.spawnFlyingParticles && Math.random() < (player.isSprinting() ? 0.02 : 0.01)) {
                    velocity = velocity.normalize().multiply(-0.25, 0.25, -0.25);
                    MinecraftClient.getInstance().particleManager.addParticle(new ColorParticleEffect(new Vector3i(color.getRed(), color.getGreen(), color.getBlue())),
                            player.getParticleX(0.5),
                            player.getRandomBodyY(),
                            player.getParticleZ(0.5),
                            velocity.x, velocity.y, velocity.z
                    );
                }
            }
        }
    }

    @Inject(method = "render(Lnet/minecraft/entity/LivingEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/entity/model/EntityModel;setAngles(Lnet/minecraft/entity/Entity;FFFFF)V", shift = At.Shift.AFTER))
    private void weightless$flyingTransforms(T entity, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumerProvider, int light, CallbackInfo ci) {
        if (entity instanceof AbstractClientPlayerEntity player && this.model instanceof PlayerEntityModel<?> playerModel) {
            if (!player.isSneaking() && WeightlessComponent.flying(player)) {
                WeightlessPosing.updateTransforms(matrices, player, tickDelta);

                boolean bl = MinecraftClient.getInstance().options.getPerspective().isFirstPerson();
                if (player != MinecraftClient.getInstance().player || !bl) {
                    WeightlessPosing.setAngles(playerModel, player, tickDelta);
                }
            }
        }
    }
}
