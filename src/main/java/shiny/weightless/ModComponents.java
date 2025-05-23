package shiny.weightless;

import org.ladysnake.cca.api.v3.component.ComponentKey;
import org.ladysnake.cca.api.v3.component.ComponentRegistry;
import org.ladysnake.cca.api.v3.entity.EntityComponentFactoryRegistry;
import org.ladysnake.cca.api.v3.entity.EntityComponentInitializer;
import org.ladysnake.cca.api.v3.entity.RespawnCopyStrategy;
import shiny.weightless.common.component.WeightlessComponent;

public class ModComponents implements EntityComponentInitializer {

    public static final ComponentKey<WeightlessComponent> WEIGHTLESS = ComponentRegistry.getOrCreate(Weightless.id("weightless"), WeightlessComponent.class);

    @Override
    public void registerEntityComponentFactories(EntityComponentFactoryRegistry registry) {
        registry.registerForPlayers(WEIGHTLESS, WeightlessComponent::new, RespawnCopyStrategy.ALWAYS_COPY);
    }
}
