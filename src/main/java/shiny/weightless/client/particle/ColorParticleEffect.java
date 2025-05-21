package shiny.weightless.client.particle;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleType;
import net.minecraft.util.dynamic.Codecs;
import org.joml.Vector3f;
import shiny.weightless.WeightlessClient;

public record ColorParticleEffect(Vector3f color) implements ParticleEffect {

    public static final MapCodec<ColorParticleEffect> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    Codecs.VECTOR_3F.fieldOf("color").forGetter(effect -> effect.color)
            ).apply(instance, ColorParticleEffect::new)
    );
    public static final PacketCodec<RegistryByteBuf, ColorParticleEffect> PACKET_CODEC = PacketCodec.tuple(
            PacketCodecs.VECTOR3F, ColorParticleEffect::color, ColorParticleEffect::new
    );

    @Override
    public ParticleType<?> getType() {
        return WeightlessClient.POINT;
    }
}
