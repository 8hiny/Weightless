package shiny.weightless.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.entity.*;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import shiny.weightless.ModConfig;
import shiny.weightless.WeightlessRenderProvider;
import shiny.weightless.common.component.WeightlessComponent;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin extends LivingEntity implements WeightlessRenderProvider {

    @Shadow public abstract void addExhaustion(float exhaustion);
    @Unique private float lastLimbAngle = 0.0f;

    protected PlayerEntityMixin(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
    }

    @ModifyReturnValue(method = "getDimensions", at = @At(value = "RETURN"))
    private EntityDimensions weightless$flyingDimensions(EntityDimensions original) {
        PlayerEntity player = (PlayerEntity) (Object) this;

        if (WeightlessComponent.flying(player) && (player.isSprinting() || WeightlessComponent.autopilot(player))) {
            return EntityDimensions.changing(0.6f, 1.5f);
        }
        return original;
    }

    @Inject(method = "increaseTravelMotionStats", at = @At(value = "HEAD"))
    private void weightless$applyExhaustionWhenFlying(double dx, double dy, double dz, CallbackInfo ci) {
        if (ModConfig.exhaust) {
            PlayerEntity player = (PlayerEntity) (Object) this;
            if (!this.hasVehicle() && WeightlessComponent.flying(player)) {
                float exhaustion = ModConfig.hungerMultiplier;

                if (ModConfig.reduceHungerWhenHigh && player.getPos().y >= ModConfig.altitude) {
                    exhaustion *= ModConfig.highHungerReduction;
                }

                float speed = (float) (dx * dx + dy * dy + dz * dz);
                if (exhaustion > 0.0f && speed >  1.0E-7) {
                    if (this.isSprinting() || WeightlessComponent.autopilot(player)) {
                        this.addExhaustion(0.04f * exhaustion);
                    }
                    else {
                        this.addExhaustion(0.001f * exhaustion);
                    }
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
    public float getLastLimbAngle() {
        return this.lastLimbAngle;
    }

    @Override
    public void setLastLimbAngle(float lastLimbAngle) {
        this.lastLimbAngle = lastLimbAngle;
    }
}
