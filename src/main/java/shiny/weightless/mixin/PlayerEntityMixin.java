package shiny.weightless.mixin;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.entity.*;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import shiny.weightless.WeightlessRenderProvider;
import shiny.weightless.common.component.WeightlessComponent;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin extends LivingEntity implements WeightlessRenderProvider {

    @Unique private float lastLimbAngle = 0.0f;

    protected PlayerEntityMixin(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
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
