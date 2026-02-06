package shiny.weightless.mixin;

import com.mojang.authlib.GameProfile;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import shiny.weightless.common.component.WeightlessComponent;
import shiny.weightless.common.config.ModConfig;
import shiny.weightless.common.util.WeightlessUtil;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerMixin extends Player {

    public ServerPlayerMixin(Level level, GameProfile gameProfile) {
        super(level, gameProfile);
    }

    @Inject(method = "checkMovementStatistics", at = @At(value = "HEAD"))
    private void weightless$applyExhaustionWhenFlying(double dx, double dy, double dz, CallbackInfo ci) {
        if (ModConfig.exhaust) {
            if (!this.isPassenger() && WeightlessComponent.flying(this)) {
                float exhaustion = ModConfig.hungerMultiplier;

                if (WeightlessUtil.canReceiveAltitudeBonus(this)) {
                    exhaustion *= ModConfig.highHungerReduction;
                }

                float speed = (float) (dx * dx + dy * dy + dz * dz);
                if (exhaustion > 0.0f && speed >  1.0E-7) {
                    if (this.isSprinting() || WeightlessComponent.inAutopilot(this)) {
                        this.causeFoodExhaustion(0.04f * exhaustion);
                    }
                    else {
                        this.causeFoodExhaustion(0.001f * exhaustion);
                    }
                }
            }
        }
    }
}
