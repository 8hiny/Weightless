package shiny.weightless;

import net.fabricmc.api.ModInitializer;

import net.minecraft.item.Item;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import shiny.weightless.common.command.WeightlessCommand;

public class Weightless implements ModInitializer {

	public static final String MOD_ID = "weightless";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	//Tags
	public static final TagKey<Item> PROJECTILE_WEAPONS = TagKey.of(RegistryKeys.ITEM, id("projectile_weapons"));

	@Override
	public void onInitialize() {
		WeightlessCommand.init();
	}

	public static Identifier id(String name) {
		return new Identifier(MOD_ID, name);
	}
}