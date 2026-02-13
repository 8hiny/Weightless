package shiny.weightless.common.component;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientWorldEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.NotNull;
import org.ladysnake.cca.api.v3.component.sync.AutoSyncedComponent;
import org.ladysnake.cca.api.v3.component.tick.ServerTickingComponent;
import shiny.weightless.common.config.ModConfig;
import shiny.weightless.common.network.ToggleAutopilotPayload;
import shiny.weightless.common.network.ToggleWeightlessPayload;
import shiny.weightless.common.util.WeightlessUtil;

public class WeightlessComponent implements AutoSyncedComponent, ServerTickingComponent {

    private static boolean updatedOnWorldLoad;
    public static boolean clientToggled;
    public static boolean clientAutopilot;

    private final Player provider;
    private boolean enabled;
    private boolean wasEnabled;
    private boolean flying;
    private boolean toggled = true;
    private boolean autopilot = false;
    private int flightTicks;
    private int remainingStunTicks;

    public WeightlessComponent(Player provider) {
        this.provider = provider;
    }

    public static void init() {
        ClientWorldEvents.AFTER_CLIENT_WORLD_CHANGE.register((client, world) -> updatedOnWorldLoad = false);
        ClientTickEvents.START_CLIENT_TICK.register(WeightlessComponent::clientTick);
    }

    public static WeightlessComponent get(@NotNull Player player) {
        return ModComponents.WEIGHTLESS.get(player);
    }

    public static boolean has(@NotNull Player player) {
        return ModComponents.WEIGHTLESS.get(player).has();
    }

    public static boolean flying(@NotNull Player player) {
        return ModComponents.WEIGHTLESS.get(player).flying();
    }

    public static boolean inAutopilot(@NotNull Player player) {
        return ModComponents.WEIGHTLESS.get(player).inAutopilot();
    }

    private void sync(boolean simplified) {
        ModComponents.WEIGHTLESS.sync(this.provider, (buf, recipient) -> this.writeSyncPacket(buf, recipient, simplified));
    }

    private static void clientTick(Minecraft client) {
        ModComponents.WEIGHTLESS.maybeGet(client.player).ifPresent(component -> {
            if (!updatedOnWorldLoad) {
                clientToggled = component.toggled;
                updatedOnWorldLoad = true;
            }
            if (component.toggled != clientToggled) {
                if (!clientToggled || !component.isStunned()) {
                    ClientPlayNetworking.send(new ToggleWeightlessPayload());
                    component.toggled = clientToggled;
                }
            }
            if (component.autopilot != clientAutopilot) {
                ClientPlayNetworking.send(new ToggleAutopilotPayload());
                component.autopilot = clientAutopilot;
            }
        });
    }

    @Override
    public void serverTick() {
        boolean sync = false;
        if (this.remainingStunTicks > 0) {
            this.remainingStunTicks--;
            sync = true;
        }

        if (this.flying) {
            this.flightTicks++;
            sync = true;
        }
        else if (this.flightTicks > 0) {
            this.flightTicks = 0;
            sync = true;
        }

        if (sync) this.sync(true);
    }

    public boolean has() {
        return this.enabled;
    }

    public boolean wasEnabled() {
        return this.wasEnabled;
    }

    public void attain() {
        this.enabled = true;
        this.wasEnabled = true;
        this.sync(false);
    }

    public void reset() {
        this.enabled = false;
        this.toggled = true;
        this.autopilot = false;
        this.flying = false;
        this.remainingStunTicks = 0;
        this.sync(false);
    }

    @Override
    public void readData(ValueInput readView) {
        this.enabled = readView.getBooleanOr("Enabled", false);
        this.wasEnabled = readView.getBooleanOr("WasEnabled", false);
        this.toggled = readView.getBooleanOr("Toggled", false);
        this.autopilot = readView.getBooleanOr("Autopilot", false);
        this.flying = readView.getBooleanOr("Flying", false);
        this.remainingStunTicks = readView.getIntOr("StunTicks", 0);
        this.flightTicks = readView.getIntOr("FlightTicks", 0);
    }

    @Override
    public void writeData(ValueOutput writeView) {
        writeView.putBoolean("Enabled", this.enabled);
        writeView.putBoolean("WasEnabled", this.wasEnabled);
        writeView.putBoolean("Toggled", this.toggled);
        writeView.putBoolean("Autopilot", this.autopilot);
        writeView.putBoolean("Flying", this.flying);
        writeView.putInt("StunTicks", this.remainingStunTicks);
        writeView.putInt("FlightTicks", this.flightTicks);
    }

    @Override
    public void applySyncPacket(RegistryFriendlyByteBuf buf) {
        if (buf.readBoolean()) {
            this.remainingStunTicks = buf.readInt();
            this.flightTicks = buf.readInt();
        }
        else {
            AutoSyncedComponent.super.applySyncPacket(buf);
        }
    }

    @Override
    public void writeSyncPacket(RegistryFriendlyByteBuf buf, ServerPlayer recipient) {
        this.writeSyncPacket(buf, recipient, false);
    }

    private void writeSyncPacket(RegistryFriendlyByteBuf buf, ServerPlayer recipient, boolean simplified) {
        buf.writeBoolean(simplified);
        if (simplified) {
            buf.writeInt(this.remainingStunTicks);
            buf.writeInt(this.flightTicks);
        }
        else {
            AutoSyncedComponent.super.writeSyncPacket(buf, recipient);
        }
    }

    public boolean isToggled() {
        return this.toggled;
    }

    public void toggle() {
        this.toggled = !this.toggled;
        this.sync(false);
    }

    public boolean inAutopilot() {
        return this.autopilot && this.provider.getFoodData().hasEnoughFood() && WeightlessUtil.canFly(this.provider);
    }

    public void toggleAutopilot() {
        this.autopilot = !autopilot;
        this.sync(false);
    }

    public boolean isStunned() {
        return this.remainingStunTicks > 0;
    }

    public void setStunned() {
        this.remainingStunTicks = ModConfig.stunDuration;
        if (this.toggled) this.toggled = false;
        this.sync(false);
    }

    public boolean flying() {
        return this.has() && this.toggled && this.flying && WeightlessUtil.canFly(this.provider);
    }

    public void setFlying(boolean flying) {
        if (flying != this.flying) {
            this.flying = flying;
            this.sync(false);
        }
    }

    public int getFlightTicks() {
        return this.flightTicks;
    }
}
