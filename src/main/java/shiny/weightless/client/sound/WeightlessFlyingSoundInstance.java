package shiny.weightless.client.sound;

import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import shiny.weightless.client.util.FlyingPlayerTracker;
import shiny.weightless.common.Weightless;
import shiny.weightless.common.component.WeightlessComponent;

public class WeightlessFlyingSoundInstance extends AbstractTickableSoundInstance {

    private final Player player;
    private final boolean local;
    private int lastAge;
    private int age;

    public WeightlessFlyingSoundInstance(Player player, boolean local) {
        super(local ? SoundEvents.ELYTRA_FLYING : Weightless.OTHER_WEIGHTLESS_FLYING, SoundSource.PLAYERS, SoundInstance.createUnseededRandom());
        this.player = player;
        this.local = local;
        this.looping = true;
        this.delay = 0;
        this.volume = 0.0f;
        this.pitch = 0.0f;
    }

    @Override
    public void tick() {
        boolean bl = false;
        if (!this.player.isRemoved() && WeightlessComponent.flying(this.player)) {
            this.x = (float) this.player.getX();
            this.y = (float) this.player.getY();
            this.z = (float) this.player.getZ();

            float speed = (float) FlyingPlayerTracker.getVelocity(this.player.getUUID()).lengthSqr();
            if (speed > 0.1f || this.player.isSprinting()) {
                float targetVolume = (this.local ? 0.25f : 1.0f) + Mth.clamp(speed / 4.0f, 0.0f, 1.0f);
                float targetPitch = 0.8f + Mth.clamp(speed / 4.0f, 0.0f, 1.0f);
                if (this.age <= 10) {
                    this.volume = Mth.lerp(0.4f, this.volume, targetVolume);
                    this.pitch = Mth.lerp(0.4f, this.pitch, targetPitch);
                }
                else {
                    this.volume = targetVolume;
                    this.pitch = targetPitch;
                }
                this.lastAge = age;
                bl = true;
            }
        }
        if (!bl && this.age > 0) {
            if (this.age - this.lastAge <= 10) {
                this.volume = Mth.lerp(0.1f, this.volume, 0.0f);
                this.pitch = Mth.lerp(0.1f, this.pitch, 0.0f);
            }
            else {
                FlyingPlayerTracker.removeSound(this.player.getUUID());
                this.stop();
            }
        }
        this.age++;
    }

    @Override
    public boolean canStartSilent() {
        return true;
    }
}
