package shiny.weightless.mixin;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import shiny.weightless.common.config.ModConfig;
import shiny.weightless.common.Weightless;
import shiny.weightless.common.component.WeightlessComponent;
import shiny.weightless.common.util.WeightlessUtil;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity {

    @Unique private int startFlyingTicks = 0;

    public LivingEntityMixin(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }

    @WrapWithCondition(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;tickHeadTurn(F)V"))
    private boolean weightless$preventBodyTurn(LivingEntity entity, float f) {
        if (entity instanceof Player player && WeightlessComponent.flying(player)) {
            entity.setYBodyRot(entity.getYHeadRot());
            return false;
        }
        return true;
    }

    @WrapWithCondition(method = "aiStep", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;calculateEntityAnimation(Z)V"))
    private boolean weightless$preventLocalWalkAnimation(LivingEntity entity, boolean bl) {
        return !(entity instanceof Player player && WeightlessComponent.flying(player));
    }

    @WrapOperation(method = "aiStep", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;travel(Lnet/minecraft/world/phys/Vec3;)V"))
    private void weightless$crosshairBasedMovement(LivingEntity entity, Vec3 movementInput, Operation<Void> original) {
        if (entity instanceof Player player && WeightlessComponent.has(player)) {
            boolean bl = WeightlessComponent.inAutopilot(player);

            boolean bl2 = !WeightlessComponent.flying(player);
            bl2 &= (entity.getXRot() < -10 && movementInput.z > 0.0) || (entity.getXRot() > 10 && movementInput.z < 0.0) || bl;

            boolean bl3 = false;
            if (this.startFlyingTicks == 0) {
                if (bl2) {
                    this.startFlyingTicks++;
                }
                else {
                    bl3 = WeightlessUtil.isCollidedWithBlock(entity);
                }
            }
            WeightlessComponent.get(player).setFlying(!bl3);

            if (WeightlessComponent.flying(player)) {
                if (bl) movementInput = new Vec3(0, 0, 1);

                float speed = WeightlessUtil.calcFlightSpeed(entity, entity.isSprinting());
                Vec3 movement = WeightlessUtil.inputToFlightVelocity(movementInput, speed, entity.getXRot(), entity.getYRot());
                Vec3 velocity = entity.getDeltaMovement().add(movement).multiply(0.85, 0.85, 0.85);

                if (entity.canSimulateMovement()) {
                    double x = velocity.x;
                    double y = velocity.y;
                    double z = velocity.z;

                    if (entity.isCrouching() || entity.isUsingItem()) y *= 0.5;

                    if (Math.abs(x) < 0.003) {
                        x = 0.0;
                    }
                    if (Math.abs(y) < 0.003) {
                        y = 0.0;
                    }
                    if (Math.abs(z) < 0.003) {
                        z = 0.0;
                    }
                    velocity = new Vec3(x, y, z);

                    entity.setDeltaMovement(velocity);
                    entity.move(MoverType.SELF, entity.getDeltaMovement());
                }

                float f = (float) (entity.getY() - entity.yOld);
                if (f < 0.0f) {
                    entity.fallDistance = Math.abs(f) * 10.0f;
                }
                else {
                    entity.fallDistance = 0.0f;
                }

                if (entity.isCrouching()) {
                    entity.calculateEntityAnimation(false);
                }
                else entity.walkAnimation.update(0.0f, 0.1f, 1.0f);

                entity.setOnGround(bl3);
            }
            else {
                original.call(entity, movementInput);
            }

            if (this.startFlyingTicks < 5 && this.startFlyingTicks > 0) this.startFlyingTicks++;
            else this.startFlyingTicks = 0;
        }
        else {
            original.call(entity, movementInput);
        }
    }

    @WrapOperation(method = "knockback", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/phys/Vec3;scale(D)Lnet/minecraft/world/phys/Vec3;"))
    private Vec3 weightless$receiveIncreasedKnockback(Vec3 vec, double scalar, Operation<Vec3> original) {
        if (ModConfig.increaseKnockback) {
            LivingEntity entity = (LivingEntity) (Object) this;
            if (entity instanceof Player player && WeightlessComponent.has(player)) {
                scalar *= ModConfig.knockbackMultiplier;
            }
        }
        return original.call(vec, scalar);
    }

    @Inject(method = "hurtServer", at = @At(value = "HEAD"))
    private void weightless$stunOnDamage(ServerLevel serverLevel, DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        if (ModConfig.stunType != ModConfig.StunType.NONE && amount >= ModConfig.damageRequirement) {
            boolean bl = ModConfig.stunType == ModConfig.StunType.ALL
                    || (ModConfig.stunType == ModConfig.StunType.PLAYER_ONLY && source.getEntity() instanceof Player)
                    || (ModConfig.stunType == ModConfig.StunType.MOB_ONLY && source.getEntity() instanceof Mob);

            if (bl) {
                LivingEntity entity = (LivingEntity) (Object) this;
                if (entity instanceof Player player && player.isAlive() && WeightlessComponent.flying(player)
                        && (source.is(Weightless.CAN_STUN) || source.getEntity() != null)) {
                    WeightlessComponent.get(player).setStunned();
                }
            }
        }
    }
}
