package shiny.weightless;

import eu.midnightdust.lib.config.MidnightConfig;

public class ModConfig extends MidnightConfig {

    public static final String CLIENT = "client";

    //Common config options
    @Entry public static int altitude = 196;
    @Entry public static boolean exhaust = true;
    @Entry(min = 0.0f) public static float hungerMultiplier = 1.0f;
    @Entry public static boolean reduceHungerWhenHigh = true;
    @Entry(min = 0.0f, max = 1.0f) public static float highHungerReduction = 0.5f;

    @Entry(min = 0.0f) public static float speedMultiplier = 1.0f;
    @Entry public static boolean movementSpeedAffectSpeed = true;
    @Entry public static boolean increaseSpeedWhenHigh = true;
    @Entry(min = 0.0f) public static float highSpeedMultiplier = 0.5f;
    @Entry public static boolean armorAffectSpeed = false;
    @Entry public static boolean itemAffectSpeed = true;

    @Entry public static boolean preventRangedWeapons = true;

    @Entry public static boolean increaseKnockback = true;
    @Entry(min = 1.0f) public static float knockbackMultiplier = 4.0f;

    @Entry public static boolean shouldStun = true;
    @Entry(min = 0.0f) public static float damageRequirement = 8.0f;
    @Entry(min = 0) public static int stunDuration = 80;

    //Client config options
    @Entry(category = CLIENT) public static boolean renderSpeedlines = true;
    @Entry(category = CLIENT) public static boolean spawnFlyingParticles = true;
    @Entry(category = CLIENT) public static boolean renderTrail = true;
    @Entry(category = CLIENT, min = 0, max = 255) public static int trailRed = 255;
    @Entry(category = CLIENT, min = 0, max = 255) public static int trailGreen = 255;
    @Entry(category = CLIENT, min = 0, max = 255) public static int trailBlue = 255;

    public static int encode() {
        String encoded = ""
                + altitude
                + exhaust
                + hungerMultiplier
                + reduceHungerWhenHigh
                + highHungerReduction
                + speedMultiplier
                + movementSpeedAffectSpeed
                + increaseSpeedWhenHigh
                + highSpeedMultiplier
                + armorAffectSpeed
                + itemAffectSpeed
                + preventRangedWeapons
                + increaseKnockback
                + knockbackMultiplier
                + shouldStun
                + damageRequirement
                + stunDuration;
        return encoded.hashCode();
    }
}
