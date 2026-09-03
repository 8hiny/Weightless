package shiny.weightless.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import shiny.weightless.common.component.WeightlessComponent;

@Mixin(ServerGamePacketListenerImpl.class)
public class ServerGamePacketListenerMixin {

    @WrapOperation(method = "handleMovePlayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;isFallFlying()Z"))
    private boolean disableFlyingMovementCheck(ServerPlayer player, Operation<Boolean> original) {
        return original.call(player) || WeightlessComponent.flying(player);
    }
}
