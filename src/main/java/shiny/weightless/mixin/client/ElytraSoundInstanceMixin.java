package shiny.weightless.mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.sound.ElytraSoundInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import shiny.weightless.common.component.WeightlessComponent;

@Mixin(ElytraSoundInstance.class)
public abstract class ElytraSoundInstanceMixin {

    @WrapOperation(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;isFallFlying()Z"))
    private boolean weightless$playFlyingSound(ClientPlayerEntity player, Operation<Boolean> original) {
        return (WeightlessComponent.flying(player) && !player.horizontalCollision && player.isSprinting()) || original.call(player);
    }
}
