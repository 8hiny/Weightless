package shiny.weightless;

import eu.midnightdust.lib.config.MidnightConfig;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.damage.DamageType;
import net.minecraft.item.Item;
import net.minecraft.network.PacketByteBuf;
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
import shiny.weightless.common.component.WeightlessComponent;

public class Weightless implements ModInitializer {

	public static final String MOD_ID = "weightless";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	//Packets
	public static final Identifier FLYING_SOUND_S2C_PACKET = id("flying_sound_packet");
	public static final Identifier COMPARE_CONFIG_MATCH_S2C_PACKET = id("compare_config_match_packet");
	public static final Identifier WEIGHTLESS_TOGGLE_C2S_PACKET = id("weightless_toggle_packet");
	public static final Identifier AUTOPILOT_TOGGLE_C2S_PACKET = id("autopilot_toggle_packet");

	//Tags
	public static final TagKey<Item> PROJECTILE_WEAPONS = TagKey.of(RegistryKeys.ITEM, id("projectile_weapons"));
	public static final TagKey<DamageType> CAN_STUN = TagKey.of(RegistryKeys.DAMAGE_TYPE, id("can_stun"));

	//Sounds
	public static final SoundEvent OTHER_WEIGHTLESS_FLYING = Registry.register(Registries.SOUND_EVENT, id("entity.weightless.flying"), SoundEvent.of(id("entity.weightless.flying")));

	@Override
	public void onInitialize() {
		WeightlessCommand.init();
		MidnightConfig.init(MOD_ID, ModConfig.class);

		ServerPlayNetworking.registerGlobalReceiver(WEIGHTLESS_TOGGLE_C2S_PACKET, (server, player, handler, buf, sender) -> {
			boolean weightlessActive = buf.readBoolean();
			WeightlessComponent.get(player).setToggled(weightlessActive);
		});
		ServerPlayNetworking.registerGlobalReceiver(AUTOPILOT_TOGGLE_C2S_PACKET, (server, player, handler, buf, sender) -> {
			boolean autopilotActive = buf.readBoolean();
			WeightlessComponent.get(player).setAutopilot(autopilotActive);
		});

		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
			PacketByteBuf buf = PacketByteBufs.create();
			buf.writeVarInt(ModConfig.encode());
			ServerPlayNetworking.send(handler.getPlayer(), COMPARE_CONFIG_MATCH_S2C_PACKET, buf);
		});

		ServerTickEvents.END_SERVER_TICK.register(server -> {
			if (server.getTicks() % 100 == 0) {
				for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
					PacketByteBuf buf = PacketByteBufs.create();
					buf.writeVarInt(ModConfig.encode());
					ServerPlayNetworking.send(player, COMPARE_CONFIG_MATCH_S2C_PACKET, buf);
				}
			}
		});
	}

	public static Identifier id(String name) {
		return new Identifier(MOD_ID, name);
	}
}