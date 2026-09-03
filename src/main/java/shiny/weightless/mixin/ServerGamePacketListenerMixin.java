package shiny.weightless.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.network.protocol.game.ServerboundChangeGameModePacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import shiny.weightless.common.component.WeightlessComponent;

@Mixin(ServerGamePacketListenerImpl.class)
public class ServerGamePacketListenerMixin {

    @Shadow public ServerPlayer player;

    @WrapOperation(method = "handleMovePlayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;isFallFlying()Z"))
    private boolean disableFlyingMovementCheck(ServerPlayer player, Operation<Boolean> original) {
        return original.call(player) || WeightlessComponent.flying(player);
    }

    @Inject(method = "handleChangeGameMode", at = @At(
            value = "INVOKE", target = "Lnet/minecraft/server/commands/GameModeCommand;setGameMode(Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/world/level/GameType;)V"
    ))
    private void updateFlyingSwitch(ServerboundChangeGameModePacket packet, CallbackInfo ci) {
        if (!this.player.gameMode.isSurvival() && packet.mode().isSurvival()) {
            WeightlessComponent.get(player).tryStartFlying();
        } else if (WeightlessComponent.flying(this.player) && !packet.mode().isSurvival()) {
            this.player.getAbilities().flying = true;
        }
    }
}
