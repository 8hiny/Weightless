package shiny.weightless.mixin.client;

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
import shiny.weightless.common.component.WeightlessComponent;

@Mixin(ClientPlayerEntity.class)
public abstract class ClientPlayerEntityMixin extends AbstractClientPlayerEntity {

    @Shadow @Final protected MinecraftClient client;

    public ClientPlayerEntityMixin(ClientWorld world, GameProfile profile) {
        super(world, profile);
    }

    @Inject(method = "tick", at = @At(value = "HEAD"))
    private void weightless$updateSprintStatus(CallbackInfo ci) {
        if (this.client.player != null && WeightlessComponent.flying(this.client.player)) {
            if (this.client.player.isSprinting() && !this.client.options.sprintKey.isPressed()) {
                this.client.player.setSprinting(false);
            }
        }
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
