package shiny.weightless.client.particle;

import net.minecraft.client.particle.*;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.MathHelper;
import org.joml.Vector3f;

public class PointParticle extends SpriteBillboardParticle {

    private final SpriteProvider spriteProvider;
    private int lastFactor = 1;

    public PointParticle(ClientWorld world, double x, double y, double z, double velocityX, double velocityY, double velocityZ, ColorParticleEffect parameters, SpriteProvider spriteProvider) {
        super(world, x, y, z, velocityX, velocityY, velocityZ);

        this.ascending = true;
        this.spriteProvider = spriteProvider;

        this.velocityX = velocityX;
        this.velocityY = velocityY;
        this.velocityZ = velocityZ;

        Vector3f color = parameters.color();
        this.red = color.x;
        this.green = color.y;
        this.blue = color.z;
        this.alpha = this.random.nextBetweenExclusive(5, 10) * 0.1f;

        this.maxAge = 30 + this.random.nextInt(20);
        this.scale = 0.25f + this.random.nextFloat() * 0.25f;

        this.setSpriteForAge(spriteProvider);
    }

    @Override
    public void tick() {
        super.tick();
        this.setSpriteForAge(this.spriteProvider);

        if (Math.random() > 0.5) {
            int factor = (int) Math.round(Math.random()) * 2 - 1;
            factor = MathHelper.lerp(0.5f, this.lastFactor, factor);
            lastFactor = factor;

            double d = factor * 0.05f + 0.05f * Math.random();
            double e = factor * 0.05f + 0.05f * Math.random();
            double f = factor * 0.05f + 0.05f * Math.random();
            this.setVelocity(
                    this.velocityX * 0.3 + d * 0.7,
                    this.velocityY * 0.3 + e * 0.7,
                    this.velocityZ * 0.3 + f * 0.7
            );
        }
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
            return new PointParticle(clientWorld, d, e, f, g, h, i, parameters, this.spriteProvider);
        }
    }
}
