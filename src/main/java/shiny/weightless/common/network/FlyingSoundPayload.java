package shiny.weightless.common.network;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import shiny.weightless.FlyingPlayerTracker;
import shiny.weightless.Weightless;
import shiny.weightless.client.sound.WeightlessFlyingSoundInstance;

public record FlyingSoundPayload(int entityId) implements CustomPayload {

    public static final CustomPayload.Id<FlyingSoundPayload> ID = new Id<>(Weightless.id("flying_sound"));
    public static final PacketCodec<PacketByteBuf, FlyingSoundPayload> CODEC = PacketCodec.tuple(PacketCodecs.VAR_INT, FlyingSoundPayload::entityId, FlyingSoundPayload::new);

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }

    public static class Handler implements ClientPlayNetworking.PlayPayloadHandler<FlyingSoundPayload> {
        @Override
        public void receive(FlyingSoundPayload payload, ClientPlayNetworking.Context context) {
            Entity entity = context.player().getWorld().getEntityById(payload.entityId);
            if (entity instanceof PlayerEntity player) {
                WeightlessFlyingSoundInstance sound = new WeightlessFlyingSoundInstance(player, player == context.player());
                FlyingPlayerTracker.startTrackingSound(player, sound);
            }
        }
    }
}
