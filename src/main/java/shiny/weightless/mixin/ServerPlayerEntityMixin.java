package shiny.weightless.mixin;

import com.mojang.authlib.GameProfile;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import shiny.weightless.ModConfig;
import shiny.weightless.common.component.WeightlessComponent;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin extends PlayerEntity {

    public ServerPlayerEntityMixin(World world, BlockPos pos, float yaw, GameProfile gameProfile) {
        super(world, pos, yaw, gameProfile);
    }

    @Inject(method = "increaseTravelMotionStats", at = @At(value = "HEAD"))
    private void weightless$applyExhaustionWhenFlying(double dx, double dy, double dz, CallbackInfo ci) {
        if (ModConfig.exhaust) {
            if (!this.hasVehicle() && WeightlessComponent.flying(this)) {
                float exhaustion = ModConfig.hungerMultiplier;

                if (ModConfig.reduceHungerWhenHigh && this.getPos().y >= ModConfig.altitude) {
                    exhaustion *= ModConfig.highHungerReduction;
                }

                float speed = (float) (dx * dx + dy * dy + dz * dz);
                if (exhaustion > 0.0f && speed >  1.0E-7) {
                    if (this.isSprinting() || WeightlessComponent.autopilot(this)) {
                        this.addExhaustion(0.04f * exhaustion);
                    }
                    else {
                        this.addExhaustion(0.001f * exhaustion);
                    }
                }
            }
        }
    }
}
