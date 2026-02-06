package shiny.weightless.common.network;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;
import shiny.weightless.common.Weightless;
import shiny.weightless.common.component.WeightlessComponent;

public record ToggleWeightlessPayload() implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<ToggleWeightlessPayload> TYPE = new CustomPacketPayload.Type<>(Weightless.id("toggle_weightless"));
    public static final StreamCodec<FriendlyByteBuf, ToggleWeightlessPayload> CODEC = StreamCodec.unit(new ToggleWeightlessPayload());

    @Override
    public Type<ToggleWeightlessPayload> type() {
        return TYPE;
    }

    public static class Handler implements ServerPlayNetworking.PlayPayloadHandler<ToggleWeightlessPayload> {
        @Override
        public void receive(ToggleWeightlessPayload payload, ServerPlayNetworking.Context context) {
            Player player = context.player();
            WeightlessComponent.get(player).toggle();
        }
    }
}
