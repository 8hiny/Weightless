package shiny.weightless.client.sound;

import net.minecraft.client.sound.MovingSoundInstance;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.MathHelper;
import shiny.weightless.VelocityTracker;
import shiny.weightless.Weightless;
import shiny.weightless.common.component.WeightlessComponent;

public class WeightlessFlyingSoundInstance extends MovingSoundInstance {

    private final PlayerEntity player;
    private final boolean self;
    private int lastAge;
    private int age;


    public WeightlessFlyingSoundInstance(PlayerEntity player, boolean self) {
        super(self ? SoundEvents.ITEM_ELYTRA_FLYING : Weightless.OTHER_WEIGHTLESS_FLYING, SoundCategory.PLAYERS, SoundInstance.createRandom());
        this.player = player;
        this.self = self;
        this.repeat = true;
        this.repeatDelay = 0;
        this.volume = self ? 0.3f : 1.0f;
        this.pitch = 0.8f;
    }

    @Override
    public void tick() {
        if (!this.player.isRemoved() && WeightlessComponent.flying(this.player) && (this.player.isSprinting() || WeightlessComponent.autopilot(this.player))) {
            this.x = (float) this.player.getX();
            this.y = (float) this.player.getY();
            this.z = (float) this.player.getZ();

            float speed = (float) (self ? this.player.getVelocity().lengthSquared() : VelocityTracker.getVelocity(this.player).lengthSquared());
            if (speed >= 1.0E-7) {
                this.volume = (self ? 0.3f : 1.0f) + MathHelper.clamp(speed / 4.0f, 0.0f, 1.0f);
                this.pitch = 0.8f + MathHelper.clamp(speed / 4.0f, 0.0f, 1.0f);
            }
            else {
                this.volume = self ? 0.3f : 1.0f;
                this.pitch = 0.8f;
            }
            this.lastAge = age;
        }
        else if (this.age > 0) {
            if (this.age - this.lastAge < 10) {
                this.volume = MathHelper.lerp(0.1f, this.volume, 0.0f);
                this.pitch = MathHelper.lerp(0.1f, this.pitch, 0.0f);
            }
            else {
                this.setDone();
            }
        }
        this.age++;
    }
}
