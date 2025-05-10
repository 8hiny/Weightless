package shiny.weightless.common.component;

import dev.onyxstudios.cca.api.v3.component.sync.AutoSyncedComponent;
import dev.onyxstudios.cca.api.v3.component.tick.CommonTickingComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;
import shiny.weightless.ModComponents;
import shiny.weightless.client.trail.Trail;

public class WeightlessComponent implements AutoSyncedComponent, CommonTickingComponent {

    private final PlayerEntity provider;
    private int remainingStunTicks;
    private boolean active;
    private final Trail trail = new Trail(2);

    public WeightlessComponent(PlayerEntity provider) {
        this.provider = provider;
    }

    public static WeightlessComponent get(@NotNull PlayerEntity player) {
        return ModComponents.WEIGHTLESS.get(player);
    }

    public static boolean has(@NotNull PlayerEntity player) {
        return ModComponents.WEIGHTLESS.get(player).has();
    }

    public static boolean flying(@NotNull PlayerEntity player) {
        return ModComponents.WEIGHTLESS.get(player).flying();
    }

    public void sync() {
        ModComponents.WEIGHTLESS.sync(provider);
    }

    @Override
    public void tick() {
        if (this.remainingStunTicks > 0) this.remainingStunTicks--;
    }

    @Override
    public void clientTick() {
        tick();

        Vec3d pos = new Vec3d(this.provider.getX(), this.provider.getBodyY(0.5), this.provider.getZ());
        this.trail.addPoint(pos);
        this.trail.tick();
    }

    public boolean has() {
        return this.active;
    }

    public void attain() {
        this.active = true;
        sync();
    }

    public void reset() {
        this.active = false;
        this.remainingStunTicks = 0;
        sync();
    }

    @Override
    public void readFromNbt(NbtCompound tag) {
        this.active = tag.getBoolean("Active");
        this.remainingStunTicks = tag.getInt("StunTicks");
    }

    @Override
    public void writeToNbt(NbtCompound tag) {
        tag.putBoolean("Active", this.active);
        tag.putInt("StunTicks", this.remainingStunTicks);
    }

    public boolean isStunned() {
        return this.remainingStunTicks > 0;
    }

    public void setStunned() {
        this.remainingStunTicks = 80;
        sync();
    }

    public boolean flying() {
        return this.has()
                && !this.provider.isSwimming()
                && !this.provider.isUsingRiptide()
                && !this.provider.isFallFlying()
                && (!this.provider.verticalCollision || this.provider.getPitch() < 0.0);
    }

    public Trail getTrail() {
        return this.trail;
    }
}
