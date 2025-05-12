package shiny.weightless;

import net.fabricmc.api.ModInitializer;

import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import shiny.weightless.common.command.WeightlessCommand;

public class Weightless implements ModInitializer {

	public static final String MOD_ID = "weightless";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	//S2C Packets
	public static final Identifier FLYING_SOUND_S2C_PACKET = id("flying_sound_packet");

	//Tags
	public static final TagKey<Item> PROJECTILE_WEAPONS = TagKey.of(RegistryKeys.ITEM, id("projectile_weapons"));

	//Sounds
	public static final SoundEvent OTHER_WEIGHTLESS_FLYING = Registry.register(Registries.SOUND_EVENT, id("entity.weightless.flying"), SoundEvent.of(id("entity.weightless.flying")));

	@Override
	public void onInitialize() {
		WeightlessCommand.init();
	}

	public static Identifier id(String name) {
		return new Identifier(MOD_ID, name);
	}
}