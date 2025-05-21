package shiny.weightless.common.network;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import org.joml.Vector3f;
import shiny.weightless.Weightless;
import shiny.weightless.common.component.WeightlessComponent;

public record UpdateTrailColorPayload(Vector3f color) implements CustomPayload {

    public static final Id<UpdateTrailColorPayload> ID = new Id<>(Weightless.id("update_trail_color"));
    public static final PacketCodec<PacketByteBuf, UpdateTrailColorPayload> CODEC = PacketCodec.tuple(PacketCodecs.VECTOR3F, UpdateTrailColorPayload::color, UpdateTrailColorPayload::new);

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }

    public static class Handler implements ServerPlayNetworking.PlayPayloadHandler<UpdateTrailColorPayload> {
        @Override
        public void receive(UpdateTrailColorPayload payload, ServerPlayNetworking.Context context) {
            PlayerEntity player = context.player();
            Vector3f color = payload.color;
            WeightlessComponent.get(player).setTrailColor((int) color.x, (int) color.y, (int) color.z);
        }
    }
}
