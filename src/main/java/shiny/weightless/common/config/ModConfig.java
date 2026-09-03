package shiny.weightless.common.config;

import eu.midnightdust.lib.config.MidnightConfig;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import shiny.weightless.common.Weightless;
import shiny.weightless.common.util.WeightlessUtil;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class ModConfig extends MidnightConfig {

    private static final String CLIENT = "client";
    private static boolean connectedToServer;

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
    @Entry public static boolean itemsAffectSpeed = true;

    @Comment(centered = true) public static Comment stun;
    @Entry public static StunType stunType = StunType.ALL;
    @Entry(min = 0.0f) public static float damageRequirement = 8.0f;
    @Entry(min = 0) public static int stunDuration = 80;

    @Comment(centered = true) public static Comment misc;
    @Entry public static boolean allPlayersWeightless = false;
    @Entry public static int altitude = 196;
    @Entry public static boolean preventRangedWeapons = true;
    @Entry public static boolean increaseKnockback = true;
    @Entry(min = 0.0f) public static float knockbackMultiplier = 3.0f;

    //Client config options //TODO Fix client options being synced via the server, fix them resetting when on a server
    @Entry(category = CLIENT) @Client public static boolean requireHoldSprint = true;
    @Entry(category = CLIENT) @Client public static boolean selfFlightSound = true;
    @Entry(category = CLIENT) @Client public static boolean legLiftedPose = true;
    @Entry(category = CLIENT) @Condition(requiredModId = "entity_model_features")
    @Client public static boolean overrideAnimations = false;

    public static boolean canReceiveAltitudeBonus(Entity entity) {
        return entity.position().y >= altitude && !WeightlessUtil.isNearBlock(entity, 8);
    }

    public static boolean shouldStun(DamageSource source, float amount) {
        return amount >= damageRequirement
                && (stunType == StunType.ALL
                || (stunType == StunType.PLAYER_ONLY && source.getEntity() instanceof Player)
                || (stunType == StunType.MOB_ONLY && source.getEntity() instanceof Mob));
    }

    public static float calcFlightSpeed(LivingEntity entity, boolean sprinting) {
        float speed = movementSpeedAffectSpeed ? entity.getSpeed() : 0.1f;
        speed *= speedMultiplier;

        if (entity.isCrouching()) {
            speed *= (float) entity.getAttributeValue(Attributes.SNEAKING_SPEED) * 2.0f;
        }
        else {
            speed *= sprinting ? 1.0f : 0.35f;
        }

        if (armorAffectSpeed) {
            speed *= Math.max(0.1f, (-0.025f * entity.getArmorValue() + 1) / armorSpeedMultiplier);
        }

        if (increaseSpeedWhenHigh && canReceiveAltitudeBonus(entity)) {
            speed *= highSpeedMultiplier;
        }
        return speed;
    }

    public static void setConnectedToServer(boolean value) {
        connectedToServer = value;
    }

    public static String encodeSettings() {
        StringBuilder builder = new StringBuilder();
        for (Field field : ModConfig.class.getFields()) {
            if (field.isAnnotationPresent(Entry.class) && !field.isAnnotationPresent(Client.class)) {
                try {
                    builder.append(field.get(null)).append(",");
                } catch (IllegalAccessException ignored) {
                }
            }
        }
        return builder.toString();
    }

    public static void decodeAndUpdateSettings(String string) {
        String[] values = string.split(",");
        List<Field> fields = getEntries(ModConfig.class.getFields());
        if (values.length == fields.size()) {
            for (int i = 0; i < values.length; i++) {
                Field field = fields.get(i);
                if (field.isAnnotationPresent(Entry.class) && !field.isAnnotationPresent(Client.class)) {
                    try {
                        updateField(field, values[i]);
                    } catch (IllegalAccessException ignored) {
                    }
                }
            }
            write(Weightless.MOD_ID);
        }
    }

    private static List<Field> getEntries(Field[] fields) {
        List<Field> list = new ArrayList<>();
        for (Field field : fields) {
            if (field.isAnnotationPresent(Entry.class) && !field.isAnnotationPresent(Client.class)) {
                list.add(field);
            }
        }
        return list;
    }

    private static void updateField(Field field, String value) throws IllegalAccessException {
        switch (field.get(null)) {
            case Integer ignored -> field.setInt(null, Integer.parseInt(value));
            case Float ignored -> field.setFloat(null, Float.parseFloat(value));
            case Boolean ignored -> field.setBoolean(null, Boolean.parseBoolean(value));
            case StunType ignored -> field.set(null, Enum.valueOf(StunType.class, value));
            default -> {}
        }
    }

    @Override
    public void writeChanges() {
        if (!connectedToServer) {
            super.writeChanges();
        }
    }

    public enum StunType {
        NONE,
        PLAYER_ONLY,
        MOB_ONLY,
        ALL
    }
}
