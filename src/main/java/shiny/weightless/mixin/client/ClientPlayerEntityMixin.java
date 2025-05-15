package shiny.weightless.mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.authlib.GameProfile;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import shiny.weightless.ModConfig;
import shiny.weightless.WeightlessClient;
import shiny.weightless.common.component.WeightlessComponent;

@Mixin(ClientPlayerEntity.class)
public abstract class ClientPlayerEntityMixin extends AbstractClientPlayerEntity {

    @Shadow @Final protected MinecraftClient client;

    public ClientPlayerEntityMixin(ClientWorld world, GameProfile profile) {
        super(world, profile);
    }

    @Inject(method = "tickMovement", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/AbstractClientPlayerEntity;tickMovement()V"))
    private void weightless$updateSprintStatus(CallbackInfo ci) {
        if (this.client.player != null && WeightlessComponent.flying(this.client.player)) {
            boolean bl = this.client.player.isUsingItem() && ModConfig.itemAffectSpeed;
            boolean bl1 = WeightlessClient.autopilotActive && !bl;
            if (!this.client.player.isSprinting() && bl1) {
                this.client.player.setSprinting(true);
            }
            else if (this.client.player.isSprinting() && !bl1 && (bl || !this.client.options.sprintKey.isPressed() || this.client.player.isSneaking())) {
                this.client.player.setSprinting(false);
            }
        }
    }

    @WrapOperation(method = "tickMovement", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;isUsingItem()Z"))
    private boolean weightless$allowSprintWithItem(ClientPlayerEntity player, Operation<Boolean> original) {
        return original.call(player) && (ModConfig.itemAffectSpeed || !WeightlessComponent.flying(player));
    }

    @WrapOperation(method = "canStartSprinting", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;isUsingItem()Z"))
    private boolean gildedglory$canStartSprintWithItem(ClientPlayerEntity player, Operation<Boolean> original) {
        return original.call(player) && (ModConfig.itemAffectSpeed || !WeightlessComponent.flying(player));
    }

    //Unused, might reuse in the future
//    @Inject(method = "tick", at = @At(value = "TAIL"))
//    private void weightless$spawnFlyingParticles(CallbackInfo ci) {
//        ClientPlayerEntity clientPlayer = this.client.player;
//
//        if (clientPlayer != null && this.getWorld().isClient()) {
//            boolean inFirstPerson = MinecraftClient.getInstance().options.getPerspective().isFirstPerson();
//
//            for (PlayerEntity checkedPlayer : clientPlayer.getWorld().getPlayers()) {
//                boolean canSpawnParticle = !checkedPlayer.getUuid().equals(clientPlayer.getUuid()) || !inFirstPerson;
//
//                if (canSpawnParticle && WeightlessComponent.flying(this) && !checkedPlayer.horizontalCollision && checkedPlayer.isSprinting() && this.age % 10 == 0) {
//                    MinecraftClient.getInstance().particleManager.addParticle(WeightlessClient.SHOCKWAVE, checkedPlayer.getX(), checkedPlayer.getY(), checkedPlayer.getZ(), 0.0, 0.0, 0.0);
//                }
//            }
//        }
//    }
}
