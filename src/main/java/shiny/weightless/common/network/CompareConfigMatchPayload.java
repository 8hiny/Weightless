package shiny.weightless.common.network;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import shiny.weightless.client.WeightlessClient;
import shiny.weightless.common.config.ModConfig;
import shiny.weightless.common.Weightless;

public record CompareConfigMatchPayload(int encoded) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<CompareConfigMatchPayload> TYPE = new CustomPacketPayload.Type<>(Weightless.id("compare_config_match"));
    public static final StreamCodec<FriendlyByteBuf, CompareConfigMatchPayload> CODEC = StreamCodec.composite(ByteBufCodecs.VAR_INT, CompareConfigMatchPayload::encoded, CompareConfigMatchPayload::new);

    @Override
    public Type<CompareConfigMatchPayload> type() {
        return TYPE;
    }

    //TODO Change this to sync the client's common config to that of the server which was joined
    public static class Handler implements ClientPlayNetworking.PlayPayloadHandler<CompareConfigMatchPayload> {
        @Override
        public void receive(CompareConfigMatchPayload payload, ClientPlayNetworking.Context context) {
            if (payload.encoded != ModConfig.encode()) {
                context.responseSender().disconnect(WeightlessClient.DISCONNECT_MESSAGE);
            }
        }
    }
}
