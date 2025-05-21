package shiny.weightless;

import eu.midnightdust.lib.config.MidnightConfig;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.damage.DamageType;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import shiny.weightless.common.command.WeightlessCommand;
import shiny.weightless.common.network.*;

public class Weightless implements ModInitializer {

	public static final String MOD_ID = "weightless";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	//Tags
	public static final TagKey<Item> PROJECTILE_WEAPONS = TagKey.of(RegistryKeys.ITEM, id("projectile_weapons"));
	public static final TagKey<DamageType> CAN_STUN = TagKey.of(RegistryKeys.DAMAGE_TYPE, id("can_stun"));

	//Sounds
	public static final SoundEvent OTHER_WEIGHTLESS_FLYING = Registry.register(Registries.SOUND_EVENT, id("entity.weightless.flying"), SoundEvent.of(id("entity.weightless.flying")));

	@Override
	public void onInitialize() {
		WeightlessCommand.init();
		MidnightConfig.init(MOD_ID, ModConfig.class);

		PayloadTypeRegistry.playS2C().register(FlyingSoundPayload.ID, FlyingSoundPayload.CODEC);
		PayloadTypeRegistry.playS2C().register(CompareConfigMatchPayload.ID, CompareConfigMatchPayload.CODEC);
		PayloadTypeRegistry.playC2S().register(ToggleWeightlessPayload.ID, ToggleWeightlessPayload.CODEC);
		PayloadTypeRegistry.playC2S().register(ToggleAutopilotPayload.ID, ToggleAutopilotPayload.CODEC);
		PayloadTypeRegistry.playC2S().register(UpdateTrailColorPayload.ID, UpdateTrailColorPayload.CODEC);

		ServerPlayNetworking.registerGlobalReceiver(ToggleWeightlessPayload.ID, new ToggleWeightlessPayload.Handler());
		ServerPlayNetworking.registerGlobalReceiver(ToggleAutopilotPayload.ID, new ToggleAutopilotPayload.Handler());
		ServerPlayNetworking.registerGlobalReceiver(UpdateTrailColorPayload.ID, new UpdateTrailColorPayload.Handler());

		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
			ServerPlayNetworking.send(handler.getPlayer(), new CompareConfigMatchPayload(ModConfig.encode()));
		});

		ServerTickEvents.END_SERVER_TICK.register(server -> {
			if (server.getTicks() % 100 == 0) {
				for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
					ServerPlayNetworking.send(player, new CompareConfigMatchPayload(ModConfig.encode()));
				}
			}
		});
	}

	public static Identifier id(String name) {
		return Identifier.of(MOD_ID, name);
	}
}