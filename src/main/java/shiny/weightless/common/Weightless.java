package shiny.weightless.common;

import eu.midnightdust.lib.config.MidnightConfig;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.item.Item;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import shiny.weightless.common.command.WeightlessCommand;
import shiny.weightless.common.component.WeightlessComponent;
import shiny.weightless.common.config.ModConfig;
import shiny.weightless.common.network.*;

public class Weightless implements ModInitializer {

	public static final String MOD_ID = "weightless";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    //Tags
    public static final TagKey<Item> PROJECTILE_WEAPONS = TagKey.create(Registries.ITEM, id("projectile_weapons"));
    public static final TagKey<DamageType> CAN_STUN = TagKey.create(Registries.DAMAGE_TYPE, id("can_stun"));

    //Sounds
    public static final SoundEvent OTHER_WEIGHTLESS_FLYING = Registry.register(BuiltInRegistries.SOUND_EVENT, id("entity.weightless.flying"), SoundEvent.createVariableRangeEvent(id("entity.weightless.flying")));

	@Override
	public void onInitialize() {
        WeightlessCommand.register();
        MidnightConfig.init(MOD_ID, ModConfig.class);

        PayloadTypeRegistry.playS2C().register(SyncConfigPayload.TYPE, SyncConfigPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(FlyingSoundPayload.TYPE, FlyingSoundPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(ToggleWeightlessPayload.TYPE, ToggleWeightlessPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(ToggleAutopilotPayload.TYPE, ToggleAutopilotPayload.CODEC);

        ServerPlayNetworking.registerGlobalReceiver(ToggleWeightlessPayload.TYPE, new ToggleWeightlessPayload.Handler());
        ServerPlayNetworking.registerGlobalReceiver(ToggleAutopilotPayload.TYPE, new ToggleAutopilotPayload.Handler());

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            if (!server.isSingleplayerOwner(handler.getPlayer().nameAndId())) {
                ServerPlayNetworking.send(handler.getPlayer(), new SyncConfigPayload(ModConfig.encodeSettings()));
            }
            WeightlessComponent component = WeightlessComponent.get(handler.player);
            if (ModConfig.allPlayersWeightless && !component.wasEnabled()) component.attain();
        });
	}

    public static Identifier id(String name) {
        return Identifier.fromNamespaceAndPath(MOD_ID, name);
    }
}