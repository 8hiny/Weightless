package shiny.weightless.client.particle;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleType;
import net.minecraft.registry.Registries;
import org.joml.Vector3f;
import org.joml.Vector3i;
import shiny.weightless.WeightlessClient;
import shiny.weightless.common.component.WeightlessComponent;

import java.util.Locale;

public class ColorParticleEffect implements ParticleEffect {

    public static final Factory<ColorParticleEffect> PARAMETERS_FACTORY = new Factory<>() {
        public ColorParticleEffect read(ParticleType<ColorParticleEffect> particleType, StringReader stringReader) throws CommandSyntaxException {
            return new ColorParticleEffect(readColor(stringReader));
        }

        public ColorParticleEffect read(ParticleType<ColorParticleEffect> particleType, PacketByteBuf packetByteBuf) {
            return new ColorParticleEffect(readColor(packetByteBuf));
        }
    };

    private final Vector3i color;

    public ColorParticleEffect(Vector3i color) {
        this.color = color;
    }

    public static Vector3i readColor(StringReader reader) throws CommandSyntaxException {
        reader.expect(' ');
        int i = reader.readInt();
        reader.expect(' ');
        int j = reader.readInt();
        reader.expect(' ');
        int k = reader.readInt();
        return new Vector3i(i, j, k);
    }

    public static Vector3i readColor(PacketByteBuf buf) {
        return new Vector3i(buf.readInt(), buf.readInt(), buf.readInt());
    }

    public Vector3i getColor() {
        return this.color;
    }
    
    @Override
    public ParticleType<?> getType() {
        return WeightlessClient.POINT;
    }

    @Override
    public void write(PacketByteBuf buf) {
        buf.writeInt(this.color.x());
        buf.writeInt(this.color.y());
        buf.writeInt(this.color.z());
    }

    @Override
    public String asString() {
        return String.format(Locale.ROOT,
                "%d %d %d",
                this.color.x(),
                this.color.y(),
                this.color.z()
        );
    }
}
