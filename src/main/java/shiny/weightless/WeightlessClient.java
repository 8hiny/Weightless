package shiny.weightless;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.minecraft.particle.DefaultParticleType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import shiny.weightless.client.particle.ShockwaveParticle;

public class WeightlessClient implements ClientModInitializer {

    public static final DefaultParticleType SHOCKWAVE = Registry.register(Registries.PARTICLE_TYPE, Weightless.id("shockwave"), FabricParticleTypes.simple());

    @Override
    public void onInitializeClient() {
        ParticleFactoryRegistry.getInstance().register(SHOCKWAVE, ShockwaveParticle.Factory::new);

    }
}
