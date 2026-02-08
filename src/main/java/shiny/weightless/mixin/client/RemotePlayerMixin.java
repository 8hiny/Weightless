package shiny.weightless.mixin.client;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.mojang.authlib.GameProfile;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.RemotePlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import shiny.weightless.common.component.WeightlessComponent;

@Mixin(RemotePlayer.class)
public abstract class RemotePlayerMixin extends AbstractClientPlayer {

    public RemotePlayerMixin(ClientLevel clientLevel, GameProfile gameProfile) {
        super(clientLevel, gameProfile);
    }

    @WrapWithCondition(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/RemotePlayer;calculateEntityAnimation(Z)V"))
    private boolean weightless$preventRemoteWalkAnimation(RemotePlayer player, boolean bl) {
        if (!player.isCrouching() && WeightlessComponent.flying(player)) {
            player.walkAnimation.update(0.0f, 0.1f, 1.0f);
            return false;
        }
        return true;
    }
}
