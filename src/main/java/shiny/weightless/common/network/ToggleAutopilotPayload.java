package shiny.weightless.common.network;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;
import shiny.weightless.common.Weightless;
import shiny.weightless.common.component.WeightlessComponent;
import shiny.weightless.common.util.WeightlessUtil;

public record ToggleAutopilotPayload() implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<ToggleAutopilotPayload> TYPE = new CustomPacketPayload.Type<>(Weightless.id("toggle_autopilot"));
    public static final StreamCodec<FriendlyByteBuf, ToggleAutopilotPayload> CODEC = StreamCodec.unit(new ToggleAutopilotPayload());

    @Override
    public Type<ToggleAutopilotPayload> type() {
        return TYPE;
    }

    public static class Handler implements ServerPlayNetworking.PlayPayloadHandler<ToggleAutopilotPayload> {
        @Override
        public void receive(ToggleAutopilotPayload payload, ServerPlayNetworking.Context context) {
            Player player = context.player();
            boolean bl = WeightlessComponent.inAutopilot(player);
            if (bl) {
                WeightlessUtil.sendFlightSoundPackets(player);
            }
            WeightlessComponent.get(player).setAutopilot(!bl);
        }
    }
}
