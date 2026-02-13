package shiny.weightless.common.config;

import eu.midnightdust.lib.config.MidnightConfig;

public class ModConfig extends MidnightConfig {

    public static final String CLIENT = "client";

    //Common config options
    @Comment(centered = true) public static Comment exhaustion;
    @Entry public static boolean exhaust = true;
    @Entry(min = 0.0f) public static float hungerMultiplier = 1.0f;
    @Entry public static boolean reduceHungerWhenHigh = true;
    @Entry(min = 0.0f, max = 1.0f) public static float highHungerReduction = 0.5f;

    @Comment(centered = true) public static Comment speed;
    @Entry(min = 0.0f) public static float speedMultiplier = 1.0f;
    @Entry public static boolean movementSpeedAffectSpeed = true;
    @Entry public static boolean increaseSpeedWhenHigh = true;
    @Entry(min = 1.0f) public static float highSpeedMultiplier = 1.5f;
    @Entry public static boolean armorAffectSpeed = false;
    @Entry(min = 1.0f) public static float armorSpeedMultiplier = 1.0f;
    @Entry public static boolean itemAffectSpeed = true;

    @Comment(centered = true) public static Comment stun;
    @Entry public static StunType stunType = StunType.ALL;
    @Entry(min = 0.0f) public static float damageRequirement = 8.0f;
    @Entry(min = 0) public static int stunDuration = 80;

    @Comment(centered = true) public static Comment misc;
    @Entry public static boolean allPlayersWeightless = false;
    @Entry public static int altitude = 196;
    @Entry public static boolean preventRangedWeapons = true;
    @Entry public static boolean increaseKnockback = true;
    @Entry(min = 0.0f) public static float knockbackMultiplier = 4.0f;

    //Client config options
    @Entry(category = CLIENT) public static boolean requireHoldSprint = true;
    @Entry(category = CLIENT) public static boolean renderSpeedlines = true;
    @Entry(category = CLIENT) public static boolean selfFlightSound = true;
    @Entry(category = CLIENT) public static boolean legLiftedPose = true;

    public static int encode() {
        String encoded = ""
                + altitude
                + allPlayersWeightless
                + exhaust
                + hungerMultiplier
                + reduceHungerWhenHigh
                + highHungerReduction
                + speedMultiplier
                + movementSpeedAffectSpeed
                + increaseSpeedWhenHigh
                + highSpeedMultiplier
                + armorAffectSpeed
                + armorSpeedMultiplier
                + itemAffectSpeed
                + preventRangedWeapons
                + increaseKnockback
                + knockbackMultiplier
                + stunType
                + damageRequirement
                + stunDuration;
        return encoded.hashCode();
    }

    public enum StunType {
        NONE,
        PLAYER_ONLY,
        MOB_ONLY,
        ALL
    }
}
