package shiny.weightless.common.network;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import shiny.weightless.Weightless;
import shiny.weightless.common.component.WeightlessComponent;

public record ToggleWeightlessPayload() implements CustomPayload {

    public static final Id<ToggleWeightlessPayload> ID = new Id<>(Weightless.id("toggle_weightless"));
    public static final PacketCodec<PacketByteBuf, ToggleWeightlessPayload> CODEC = PacketCodec.unit(new ToggleWeightlessPayload());

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }

    public static class Handler implements ServerPlayNetworking.PlayPayloadHandler<ToggleWeightlessPayload> {
        @Override
        public void receive(ToggleWeightlessPayload payload, ServerPlayNetworking.Context context) {
            PlayerEntity player = context.player();
            boolean bl = WeightlessComponent.get(player).toggled();
            WeightlessComponent.get(player).setToggled(!bl);
        }
    }
}
