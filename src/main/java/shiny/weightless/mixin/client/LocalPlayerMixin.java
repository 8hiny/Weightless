package shiny.weightless.mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.authlib.GameProfile;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.item.component.UseEffects;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import shiny.weightless.client.WeightlessClient;
import shiny.weightless.common.config.ModConfig;
import shiny.weightless.common.component.WeightlessComponent;

@Mixin(LocalPlayer.class)
public abstract class LocalPlayerMixin extends AbstractClientPlayer {

    @Shadow public abstract boolean isUsingItem();
    @Shadow protected abstract boolean isSlowDueToUsingItem();

    public LocalPlayerMixin(ClientLevel clientLevel, GameProfile gameProfile) {
        super(clientLevel, gameProfile);
    }

    @Inject(method = "aiStep", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/AbstractClientPlayer;aiStep()V"))
    private void weightless$requireHoldSprint(CallbackInfo ci) {
        if (WeightlessComponent.flying(this)) {
            boolean bl = WeightlessClient.AUTOPILOT.isDown() || this.isSlowDueToUsingItem();
            if (!bl && this.isSprinting() && !Minecraft.getInstance().options.keySprint.isDown()) {
                this.setSprinting(false);
            }
        }
    }

    @WrapOperation(method = "isSlowDueToUsingItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/component/UseEffects;canSprint()Z"))
    private boolean weightless$allowSpringWhileUsingItem(UseEffects useEffects, Operation<Boolean> original) {
        return original.call(useEffects) && (ModConfig.itemAffectSpeed || !WeightlessComponent.flying(this));
    }
}
