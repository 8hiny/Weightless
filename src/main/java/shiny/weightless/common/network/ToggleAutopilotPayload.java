package shiny.weightless.common.network;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import shiny.weightless.Weightless;
import shiny.weightless.common.component.WeightlessComponent;

public record ToggleAutopilotPayload() implements CustomPayload {

    public static final Id<ToggleAutopilotPayload> ID = new Id<>(Weightless.id("toggle_autopilot"));
    public static final PacketCodec<PacketByteBuf, ToggleAutopilotPayload> CODEC = PacketCodec.unit(new ToggleAutopilotPayload());

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }

    public static class Handler implements ServerPlayNetworking.PlayPayloadHandler<ToggleAutopilotPayload> {
        @Override
        public void receive(ToggleAutopilotPayload payload, ServerPlayNetworking.Context context) {
            PlayerEntity player = context.player();
            boolean bl = WeightlessComponent.get(player).autopilot();
            WeightlessComponent.get(player).setAutopilot(!bl);
        }
    }
}
