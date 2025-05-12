package shiny.weightless.mixin.client;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.sound.ElytraSoundInstance;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import shiny.weightless.WeightlessClient;
import shiny.weightless.common.component.WeightlessComponent;

@Mixin(ClientPlayerEntity.class)
public abstract class ClientPlayerEntityMixin extends AbstractClientPlayerEntity {

    @Shadow @Final protected MinecraftClient client;
    @Unique private boolean startedSprinting = false;
    @Unique private boolean hasPlayedSound = false;

    public ClientPlayerEntityMixin(ClientWorld world, GameProfile profile) {
        super(world, profile);
    }

    @Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/AbstractClientPlayerEntity;tick()V"))
    private void weightless$sprintStatus(CallbackInfo ci) {
        if (WeightlessComponent.has(this) && this.isSprinting() && !this.client.options.sprintKey.isPressed()) {
            this.setSprinting(false);
        }
        if (!this.hasPlayedSound && WeightlessComponent.has(this) && (!this.isSprinting() || this.isOnGround()) && !this.horizontalCollision && this.client.options.sprintKey.isPressed()) {
            this.startedSprinting = true;
        }
        this.hasPlayedSound = false;
    }

    @Inject(method = "tickMovement", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/AbstractClientPlayerEntity;tickMovement()V", shift = At.Shift.AFTER))
    private void weightless$playFlyingSound(CallbackInfo ci) {
        if (this.startedSprinting) {
            if (WeightlessComponent.has(this) && !this.horizontalCollision && this.isSprinting()) {
                ClientPlayerEntity player = (ClientPlayerEntity) (Object) this;
                this.client.getSoundManager().play(new ElytraSoundInstance(player));
                this.startedSprinting = false;
                this.hasPlayedSound = true;
            }
        }
    }

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

    @Override
    public void setSprinting(boolean sprinting) {
        if (sprinting && WeightlessComponent.flying(this) && this.horizontalCollision) sprinting = false;
        super.setSprinting(sprinting);
    }
}
