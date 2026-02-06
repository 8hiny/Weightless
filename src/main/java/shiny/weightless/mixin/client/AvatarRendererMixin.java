package shiny.weightless.mixin.client;

import net.minecraft.client.entity.ClientAvatarEntity;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.world.entity.Avatar;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import shiny.weightless.client.model.RenderStateDataKeys;
import shiny.weightless.common.component.WeightlessComponent;
import shiny.weightless.common.util.FlyingPlayerTracker;

@Mixin(AvatarRenderer.class)
public abstract class AvatarRendererMixin<AvatarlikeEntity extends Avatar & ClientAvatarEntity>
        extends LivingEntityRenderer<AvatarlikeEntity, AvatarRenderState, PlayerModel> {

    public AvatarRendererMixin(EntityRendererProvider.Context context, PlayerModel entityModel, float f) {
        super(context, entityModel, f);
    }

    @Inject(method = "extractRenderState(Lnet/minecraft/world/entity/Avatar;Lnet/minecraft/client/renderer/entity/state/AvatarRenderState;F)V", at = @At(value = "TAIL"))
    private void weightless$updateRenderdata(AvatarlikeEntity avatar, AvatarRenderState state, float tickDelta, CallbackInfo ci) {
        if (avatar instanceof Player player) {
            state.setData(RenderStateDataKeys.WEIGHTLESS_FLYING, WeightlessComponent.flying(player));
            state.setData(RenderStateDataKeys.IS_SPRINTING, player.isSprinting());
            state.setData(RenderStateDataKeys.IS_LOCAL_PLAYER, player.isLocalPlayer());
            state.setData(RenderStateDataKeys.FLIGHT_TICKS, WeightlessComponent.get(player).getFlightTicks());
            state.setData(RenderStateDataKeys.YAW, player.getYRot(tickDelta));
            state.setData(RenderStateDataKeys.VELOCITY, FlyingPlayerTracker.getLerpedVelocity(player.getUUID(), tickDelta));
        }
    }
}
