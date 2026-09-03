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
    private boolean preventBodyTurn(LivingEntity entity, float f) {
        if (entity instanceof Player player && WeightlessComponent.flying(player)) {
            entity.setYBodyRot(entity.getYHeadRot());
            return false;
        }
        return true;
    }

    @WrapWithCondition(method = "aiStep", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;calculateEntityAnimation(Z)V"))
    private boolean preventLocalWalkAnimation(LivingEntity entity, boolean bl) {
        return !(entity instanceof Player player && WeightlessComponent.flying(player));
    }

    @WrapOperation(method = "aiStep", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;travel(Lnet/minecraft/world/phys/Vec3;)V"))
    private void crosshairBasedMovement(LivingEntity entity, Vec3 movementInput, Operation<Void> original) {
        if (!(entity instanceof Player player) || !WeightlessComponent.has(player)) {
            original.call(entity, movementInput);
            return;
        }

        boolean inAutopilot = WeightlessComponent.inAutopilot(player);
        boolean takingOff = !WeightlessComponent.flying(player);
        takingOff &= inAutopilot || (player.getXRot() < -10 && movementInput.z > 0.0)
                || (player.getXRot() > 10 && movementInput.z < 0.0);

        boolean bl3 = false;
        if (this.startFlyingTicks == 0) {
            if (takingOff) {
                this.startFlyingTicks++;
            }
            else {
                bl3 = WeightlessUtil.isCollidedWithBlock(player);
            }
        }
        WeightlessComponent.get(player).setFlying(!bl3);

        if (WeightlessComponent.flying(player)) {
            if (inAutopilot) movementInput = new Vec3(movementInput.x, movementInput.y, 1);
            WeightlessUtil.affectForFlight(player, movementInput);

            if (player.level().isClientSide()) {
                player.calculateEntityAnimation(false);
            }
            player.setOnGround(bl3);
        }
        else {
            original.call(player, movementInput);
        }

        if (this.startFlyingTicks < 5 && this.startFlyingTicks > 0) this.startFlyingTicks++;
        else this.startFlyingTicks = 0;
    }

    @WrapOperation(method = "knockback", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/phys/Vec3;scale(D)Lnet/minecraft/world/phys/Vec3;"))
    private Vec3 receiveIncreasedKnockback(Vec3 vec, double scalar, Operation<Vec3> original) {
        if (ModConfig.increaseKnockback) {
            LivingEntity entity = (LivingEntity) (Object) this;
            if (entity instanceof Player player && WeightlessComponent.has(player)) {
                scalar *= ModConfig.knockbackMultiplier;
            }
        }
        return original.call(vec, scalar);
    }

    @Inject(method = "hurtServer", at = @At(value = "TAIL"))
    private void stunOnDamage(ServerLevel serverLevel, DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        if (ModConfig.shouldStun(source, amount)) {
            LivingEntity entity = (LivingEntity) (Object) this;
            if (entity instanceof Player player && player.isAlive() && WeightlessComponent.flying(player)
                    && (source.is(Weightless.CAN_STUN) || source.getEntity() != null)) {
                WeightlessComponent.get(player).setStunned();
            }
        }
    }
}
