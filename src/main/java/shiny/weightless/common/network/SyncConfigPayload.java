package shiny.weightless.common.network;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import shiny.weightless.common.config.ModConfig;
import shiny.weightless.common.Weightless;

public record SyncConfigPayload(String encoded) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<SyncConfigPayload> TYPE = new CustomPacketPayload.Type<>(Weightless.id("sync_config"));
    public static final StreamCodec<FriendlyByteBuf, SyncConfigPayload> CODEC = StreamCodec.composite(ByteBufCodecs.STRING_UTF8, SyncConfigPayload::encoded, SyncConfigPayload::new);

    @Override
    public Type<SyncConfigPayload> type() {
        return TYPE;
    }

    public static class Handler implements ClientPlayNetworking.PlayPayloadHandler<SyncConfigPayload> {
        @Override
        public void receive(SyncConfigPayload payload, ClientPlayNetworking.Context context) {
            ModConfig.decodeAndUpdateSettings(payload.encoded);
            ModConfig.setConnectedToServer(true);
        }
    }
}
