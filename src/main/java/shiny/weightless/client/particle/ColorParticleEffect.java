package shiny.weightless.client.particle;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleType;
import org.joml.Vector3f;
import org.joml.Vector3i;

import java.util.Locale;

public class ColorParticleEffect implements ParticleEffect {

    public static final Factory<ColorParticleEffect> PARAMETERS_FACTORY = new Factory<>() {
        public ColorParticleEffect read(ParticleType<ColorParticleEffect> particleType, StringReader stringReader) throws CommandSyntaxException {
            return new ColorParticleEffect(particleType, readColor(stringReader));
        }

        public ColorParticleEffect read(ParticleType<ColorParticleEffect> particleType, PacketByteBuf packetByteBuf) {
            return new ColorParticleEffect(particleType, readColor(packetByteBuf));
        }
    };

    private final ParticleType<ColorParticleEffect> type;
    private final Vector3f color;

    public ColorParticleEffect(ParticleType<ColorParticleEffect> type, Vector3i color) {
        this.type = type;
        this.color = new Vector3f((float) color.x / 255, (float) color.y / 255, (float) color.z / 255);
    }

    public ColorParticleEffect(ParticleType<ColorParticleEffect> type, Vector3f color) {
        this.type = type;
        this.color = color;
    }

    public static Vector3f readColor(StringReader reader) throws CommandSyntaxException {
        reader.expect(' ');
        float f = reader.readFloat();
        reader.expect(' ');
        float g = reader.readFloat();
        reader.expect(' ');
        float h = reader.readFloat();
        return new Vector3f(f, g, h);
    }

    public static Vector3f readColor(PacketByteBuf buf) {
        return new Vector3f(buf.readFloat(), buf.readFloat(), buf.readFloat());
    }

    public Vector3f getColor() {
        return this.color;
    }
    
    @Override
    public ParticleType<?> getType() {
        return this.type;
    }

    @Override
    public void write(PacketByteBuf buf) {
        buf.writeFloat(this.color.x());
        buf.writeFloat(this.color.y());
        buf.writeFloat(this.color.z());
    }

    @Override
    public String asString() {
        return String.format(Locale.ROOT,
                "%.2f %.2f %.2f",
                this.color.x(),
                this.color.y(),
                this.color.z()
        );
    }
}
