package shiny.weightless;

import eu.midnightdust.lib.config.MidnightConfig;

import java.util.Arrays;
import java.util.List;

public class ModConfig extends MidnightConfig {

    public static final String CLIENT = "client";

    //Common config options
    @Entry public static boolean limitPlayers = false;
    @Entry(min = -1) public static int playerLimit = -1;

    @Entry public static int altitude = 200;
    @Entry public static boolean exhaust = true;
    @Entry(min = 0.0f) public static float hungerMultiplier = 1.0f;
    @Entry public static boolean reduceHungerWhenHigh = true;
    @Entry(min = 0.0f, max = 1.0f) public static float highHungerReduction = 0.5f;

    @Entry public static boolean movementSpeedAffectSpeed = true;
    @Entry public static boolean increaseSpeedWhenHigh = true;
    @Entry(min = 0.0f) public static float highSpeedMultiplier = 0.25f;
    @Entry public static boolean armorAffectSpeed = false;
    @Entry public static boolean itemAffectSpeed = true;

    @Entry public static boolean shouldStun;
    @Entry public static boolean stunDamageAffectedByModifiers = false;
    @Entry(min = 0.0f) public static float damageRequirement = 8.0f;
    @Entry public static List<String> stunDamageTypes = Arrays.asList(
            "minecraft:is_player_attack",
            "minecraft:is_projectile",
            "minecraft:is_explosion",
            "minecraft:bypasses_invulnerability"
    );

    //Client config options
    @Entry(category = CLIENT) public static boolean renderTrail = true;
    @Entry(category = CLIENT, min = 0, max = 255) public static int trailRed = 255;
    @Entry(category = CLIENT, min = 0, max = 255) public static int trailGreen = 255;
    @Entry(category = CLIENT, min = 0, max = 255) public static int trailBlue = 255;
}
