package shiny.weightless.common.network;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import shiny.weightless.ModConfig;
import shiny.weightless.Weightless;
import shiny.weightless.WeightlessClient;

public record CompareConfigMatchPayload(int encoded) implements CustomPayload {

    public static final Id<CompareConfigMatchPayload> ID = new Id<>(Weightless.id("compare_config_match"));
    public static final PacketCodec<PacketByteBuf, CompareConfigMatchPayload> CODEC = PacketCodec.tuple(PacketCodecs.VAR_INT, CompareConfigMatchPayload::encoded, CompareConfigMatchPayload::new);

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }

    public static class Handler implements ClientPlayNetworking.PlayPayloadHandler<CompareConfigMatchPayload> {
        @Override
        public void receive(CompareConfigMatchPayload payload, ClientPlayNetworking.Context context) {
            if (payload.encoded != ModConfig.encode()) {
                context.responseSender().disconnect(WeightlessClient.DISCONNECT_MESSAGE);
            }
        }
    }
}
