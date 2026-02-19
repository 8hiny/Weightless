package shiny.weightless.client.render;

import net.fabricmc.fabric.api.client.rendering.v1.RenderStateDataKey;
import net.minecraft.world.phys.Vec3;

public class RenderStateDataKeys {

    //Entity render state keys
    public static final RenderStateDataKey<Boolean> WEIGHTLESS_FLYING = RenderStateDataKey.create(() -> "weightless_flying");
    public static final RenderStateDataKey<Boolean> IS_LOCAL_PLAYER = RenderStateDataKey.create(() -> "is_local_player");
    public static final RenderStateDataKey<Boolean> IN_WORLD = RenderStateDataKey.create(() -> "in_world");
    public static final RenderStateDataKey<Boolean> IS_SHINY = RenderStateDataKey.create(() -> "is_shiny");
    public static final RenderStateDataKey<Integer> FLIGHT_TICKS = RenderStateDataKey.create(() -> "flight_ticks");
    public static final RenderStateDataKey<Vec3> VELOCITY = RenderStateDataKey.create(() -> "velocity");
    public static final RenderStateDataKey<Float> PITCH = RenderStateDataKey.create(() -> "pitch");

    //Level render state keys
    public static final RenderStateDataKey<Boolean> HAS_SHINY_ENTITY = RenderStateDataKey.create(() -> "has_shiny_entity");
}
