package shiny.weightless.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import shiny.weightless.common.component.WeightlessComponent;

@Mixin(Entity.class)
public abstract class EntityMixin {

    @WrapWithCondition(method = "move", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;fall(DZLnet/minecraft/block/BlockState;Lnet/minecraft/util/math/BlockPos;)V"))
    private boolean weightless$preventFall(Entity entity, double heightDifference, boolean onGround, BlockState state, BlockPos landedPosition) {
        if (entity instanceof PlayerEntity player && WeightlessComponent.flying(player)) {
            return false;
        }
        else return true;
    }

    @ModifyReturnValue(method = "getStandingEyeHeight", at = @At(value = "RETURN"))
    private float observations$weightlessFlyingEyeHeight(float original) {
        Entity entity = (Entity) (Object) this;

        if (entity instanceof PlayerEntity player && WeightlessComponent.flying(player) && (player.isSprinting() || WeightlessComponent.autopilot(player))) {
            original = 1.275f;
        }
        return original;
    }
}
