package shiny.weightless.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.entity.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import shiny.weightless.common.component.WeightlessComponent;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity {

    @Shadow public abstract float getMovementSpeed();
    @Shadow @Final public LimbAnimator limbAnimator;

    public LivingEntityMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    @WrapOperation(method = "tickMovement", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;travel(Lnet/minecraft/util/math/Vec3d;)V"))
    private void weightless$crosshairBasedMovement(LivingEntity entity, Vec3d movementInput, Operation<Void> original) {
        float speed = this.getMovementSpeed() * (this.isSprinting() ? 1.0f : 0.35f);
        Vec3d movement = weightlessMovement(movementInput, speed, this.getPitch(), this.getYaw());
        Vec3d velocity = this.getVelocity().add(movement).multiply(0.85, 0.85, 0.85);

        Vec3d vec3d = ((EntityAccessor) this).weightless$adjustMovementForCollisions(velocity);
        this.verticalCollision = velocity.y != vec3d.y;

        if (entity instanceof PlayerEntity player && WeightlessComponent.flying(player)) {
            if (this.isLogicalSideForUpdatingMovement()) {
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
                velocity = new Vec3d(x, y, z);

                this.setVelocity(velocity);
                this.move(MovementType.SELF, this.getVelocity());
            }
            this.limbAnimator.updateLimbs(0.0f, 0.1f);
        }
        else {
            original.call(entity, movementInput);
        }
    }

    //Somehow stops knockback dealt from projectiles but works on melee attacks
    @WrapOperation(method = "takeKnockback", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/math/Vec3d;multiply(D)Lnet/minecraft/util/math/Vec3d;"))
    private Vec3d weightless$receiveIncreasedKnockback(Vec3d vector, double value, Operation<Vec3d> original) {
        LivingEntity entity = (LivingEntity) (Object) this;
        if (entity instanceof PlayerEntity player && WeightlessComponent.has(player)) {
            value *= 4.0f;
        }
        return original.call(vector, value);
    }

    @Unique
    private static Vec3d weightlessMovement(Vec3d movementInput, float speed, float pitch, float yaw) {
        double d = movementInput.lengthSquared();
        if (d < 1.0E-7) {
            return Vec3d.ZERO;
        }
        else {
            Vec3d vec3d = (d > 1.0 ? movementInput.normalize() : movementInput).multiply(speed);
            float f = MathHelper.sin(yaw * (float) (Math.PI / 180.0));
            float g = MathHelper.cos(yaw * (float) (Math.PI / 180.0));
            float h = -MathHelper.sin(pitch * (float) (Math.PI / 180.0));

            return new Vec3d(
                    vec3d.x * g - vec3d.z * f,
                    (vec3d.z > 0 ? h : -h) * speed * (movementInput.z != 0 ? 1.1 : 0),
                    vec3d.z * g + vec3d.x * f
            );
        }
    }
}
