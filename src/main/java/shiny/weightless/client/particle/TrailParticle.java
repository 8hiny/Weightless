package shiny.weightless.client.particle;

import net.minecraft.client.particle.*;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.world.ClientWorld;
import org.joml.Vector3f;

public class TrailParticle extends SpriteBillboardParticle {

    private final SpriteProvider spriteProvider;
    private final Vector3f color;

    public TrailParticle(ClientWorld world, double x, double y, double z, double velocityX, double velocityY, double velocityZ, ColorParticleEffect parameters, SpriteProvider spriteProvider) {
        super(world, x, y, z, velocityX, velocityY, velocityZ);
        this.velocityMultiplier = 0.96f;
        this.spriteProvider = spriteProvider;

        this.velocityX *= 0.2f;
        this.velocityY *= 0.2f;
        this.velocityZ *= 0.2f;
        this.velocityX += velocityX;
        this.velocityY += velocityY;
        this.velocityZ += velocityZ;

        Vector3f color = parameters.getColor();
        float f = this.random.nextFloat() * 0.4f + 0.6f;
        this.color = color;
        this.red = this.darken(color.x(), f);
        this.green = this.darken(color.y(), f);
        this.blue = this.darken(color.z(), f);

        this.scale *= 1.875f;
        int i = (int)(8.0 / (Math.random() * 0.8 + 0.3));
        this.maxAge = (int)Math.max(i * 2.5F, 1.0F);
        this.collidesWithWorld = false;

        this.setSpriteForAge(spriteProvider);
    }

    protected float darken(float colorComponent, float multiplier) {
        return (this.random.nextFloat() * 0.2F + 0.8F) * colorComponent * multiplier;
    }

    @Override
    public void tick() {
        super.tick();
        this.setSpriteForAge(this.spriteProvider);
    }

    @Override
    public void buildGeometry(VertexConsumer vertexConsumer, Camera camera, float tickDelta) {
        this.updateColor(tickDelta);
        super.buildGeometry(vertexConsumer, camera, tickDelta);
    }

    private void updateColor(float tickDelta) {
        float f = (this.age + tickDelta) / (this.maxAge + 1.0f);
        Vector3f vector3f = new Vector3f(1.0f, 1.0f, 1.0f).lerp(this.color, f);
        this.red = vector3f.x();
        this.green = vector3f.y();
        this.blue = vector3f.z();
    }

    @Override
    public int getBrightness(float tint) {
        return 15728880;
    }

    @Override
    public ParticleTextureSheet getType() {
        return ParticleTextureSheet.PARTICLE_SHEET_TRANSLUCENT;
    }

    public static class Factory implements ParticleFactory<ColorParticleEffect> {

        private final SpriteProvider spriteProvider;

        public Factory(SpriteProvider spriteProvider) {
            this.spriteProvider = spriteProvider;
        }

        public Particle createParticle(ColorParticleEffect parameters, ClientWorld clientWorld, double d, double e, double f, double g, double h, double i) {
            return new TrailParticle(clientWorld, d, e, f, g, h, i, parameters, this.spriteProvider);
        }
    }
}
