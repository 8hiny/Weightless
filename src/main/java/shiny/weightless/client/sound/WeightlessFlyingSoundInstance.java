package shiny.weightless.client.sound;

import net.minecraft.client.sound.MovingSoundInstance;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.MathHelper;
import shiny.weightless.Weightless;
import shiny.weightless.common.component.WeightlessComponent;

public class WeightlessFlyingSoundInstance extends MovingSoundInstance {

    private final PlayerEntity player;
    private final boolean self;
    private int tickCount;

    public WeightlessFlyingSoundInstance(PlayerEntity player, boolean self) {
        super(self ? SoundEvents.ITEM_ELYTRA_FLYING : Weightless.OTHER_WEIGHTLESS_FLYING, SoundCategory.PLAYERS, SoundInstance.createRandom());
        this.player = player;
        this.self = self;
        this.repeat = true;
        this.repeatDelay = 0;
        this.volume = 0.1f;
    }

    @Override
    public void tick() {
        this.tickCount++;
        if (!this.player.isRemoved() && WeightlessComponent.flying(player) && player.isSprinting()) {
            this.x = (float) this.player.getX();
            this.y = (float) this.player.getY();
            this.z = (float) this.player.getZ();

            if (this.self) {
                float f = (float) this.player.getVelocity().lengthSquared();
                if (f >= 1.0E-7) {
                    this.volume = MathHelper.clamp(f / 4.0f, 0.0f, 1.0f);
                } else {
                    this.volume = 0.0f;
                }
            }
            else {
                this.volume = 1.0f;
            }

            if (this.tickCount < 20) {
                this.volume = 0.0f;
            }
            else if (this.tickCount < 40) {
                this.volume = this.volume * ((this.tickCount - 20) / 20.0f);
            }

            if (this.volume > 0.8f) {
                this.pitch = 1.0f + (this.volume - 0.8f);
            }
            else {
                this.pitch = 1.0f;
            }
        }
        else if (this.tickCount > 0) {
            this.setDone();
        }
    }
}
