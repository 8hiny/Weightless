package shiny.weightless.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.Flutterer;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.tag.DamageTypeTags;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import shiny.weightless.WeightlessRenderProvider;
import shiny.weightless.common.component.WeightlessComponent;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin extends LivingEntity implements WeightlessRenderProvider {

    @Shadow public abstract void addExhaustion(float exhaustion);
    @Unique private float leanAngle = 0.0f;
    @Unique private float tickDelta = 0.0f;
    @Unique private Vec3d lastMovement = Vec3d.ZERO;

    protected PlayerEntityMixin(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
    }

    @Inject(method = "damage", at = @At(value = "TAIL"))
    private void weightless$stunOnDamage(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        PlayerEntity player = (PlayerEntity) (Object) this;

        if (amount >= 8 && !player.isInvulnerableTo(source) && !source.isIn(DamageTypeTags.BYPASSES_INVULNERABILITY) && player.isAlive() && WeightlessComponent.has(player)) {
            WeightlessComponent.get(player).setStunned();
        }
    }

    @ModifyReturnValue(method = "isImmobile", at = @At(value = "RETURN"))
    private boolean weightless$isImmobile(boolean original) {
        PlayerEntity player = (PlayerEntity) (Object) this;
        return original || WeightlessComponent.get(player).isStunned();
    }

    @WrapWithCondition(method = "travel", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;travel(Lnet/minecraft/util/math/Vec3d;)V"))
    private boolean weightless$preventMovement(LivingEntity instance, Vec3d movementInput) {
        PlayerEntity player = (PlayerEntity) (Object) this;
        if (WeightlessComponent.get(player).isStunned()) {
            player.updateLimbs(player instanceof Flutterer);
            return false;
        }
        return true;
    }

    @Inject(method = "increaseTravelMotionStats", at = @At(value = "HEAD"))
    private void weightless$applyExhaustionWhenFlying(double dx, double dy, double dz, CallbackInfo ci) {
        PlayerEntity player = (PlayerEntity) (Object) this;

        if (!this.hasVehicle() && WeightlessComponent.flying(player)) {

            int i = Math.round((float) Math.sqrt(dx * dx + dz * dz) * 100.0f);
            if (i > 0) {
                if (this.isSprinting()) {
                    this.addExhaustion(0.1f * (float) i * 0.007f);
                }
                else {
                    this.addExhaustion(0.0f * (float) i * 0.01f);
                }
            }
        }
    }

    @Inject(method = "handleFallDamage", at = @At(value = "HEAD"), cancellable = true)
    private void weightless$preventFallDamage(float fallDistance, float damageMultiplier, DamageSource damageSource, CallbackInfoReturnable<Boolean> cir) {
        PlayerEntity player = (PlayerEntity) (Object) this;

        if (WeightlessComponent.has(player)) {
            cir.setReturnValue(false);
        }
    }

    @WrapWithCondition(method = "attack", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;setSprinting(Z)V"))
    private boolean weightless$staySprinting(PlayerEntity player, boolean value) {
        return !WeightlessComponent.flying(player);
    }

    @WrapOperation(method = "getBlockBreakingSpeed", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;isOnGround()Z"))
    private boolean weightless$sameBreakingSpeed(PlayerEntity player, Operation<Boolean> original) {
        return original.call(player) || WeightlessComponent.flying(player);
    }

    @Override
    public float weightless$getLeanAngle() {
        return this.leanAngle;
    }

    @Override
    public void weightless$setLeanAngle(float angle) {
        this.leanAngle = angle;
    }

    @Override
    public float getTickDelta() {
        return this.tickDelta;
    }

    @Override
    public void setTickDelta(float tickDelta) {
        this.tickDelta = tickDelta;
    }

    @Override
    public Vec3d getLastMovement() {
        return this.lastMovement;
    }

    @Override
    public void setLastMovement(Vec3d lastMovement) {
        this.lastMovement = lastMovement;
    }
}
