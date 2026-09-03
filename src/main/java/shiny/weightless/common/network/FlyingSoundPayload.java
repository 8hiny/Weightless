package shiny.weightless.common.network;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import shiny.weightless.client.sound.WeightlessSoundInstance;
import shiny.weightless.client.util.PlayerFlightTracker;
import shiny.weightless.common.Weightless;

public record FlyingSoundPayload(int entityId) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<FlyingSoundPayload> TYPE = new CustomPacketPayload.Type<>(Weightless.id("flying_sound"));
    public static final StreamCodec<FriendlyByteBuf, FlyingSoundPayload> CODEC = StreamCodec.composite(ByteBufCodecs.VAR_INT, FlyingSoundPayload::entityId, FlyingSoundPayload::new);

    @Override
    public Type<FlyingSoundPayload> type() {
        return TYPE;
    }

    public static class Handler implements ClientPlayNetworking.PlayPayloadHandler<FlyingSoundPayload> {
        @Override
        public void receive(FlyingSoundPayload payload, ClientPlayNetworking.Context context) {
            Entity entity = context.player().level().getEntity(payload.entityId);
            if (entity instanceof Player player) {
                PlayerFlightTracker.getInstance().startTrackingSound(
                        Minecraft.getInstance(), new WeightlessSoundInstance(player, player == context.player()),
                        player
                );
            }
        }
    }
}
