package shiny.weightless.mixin;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import shiny.weightless.common.config.ModConfig;
import shiny.weightless.common.Weightless;
import shiny.weightless.common.component.WeightlessComponent;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin {

    @Inject(method = "use", at = @At(value = "HEAD"), cancellable = true)
    private void preventItemUse(Level level, Player player, InteractionHand hand, CallbackInfoReturnable<InteractionResult> cir) {
        if (ModConfig.preventRangedWeapons) {
            ItemStack stack = player.getItemInHand(hand);
            if (WeightlessComponent.flying(player) && stack.is(Weightless.PROJECTILE_WEAPONS)) {
                cir.setReturnValue(InteractionResult.FAIL);
            }
        }
    }

    @WrapWithCondition(method = "onUseTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/Item;onUseTick(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/item/ItemStack;I)V"))
    private boolean preventItemUsageTick(Item instance, Level level, LivingEntity entity, ItemStack stack, int i) {
        if (ModConfig.preventRangedWeapons && entity instanceof Player player
                && WeightlessComponent.flying(player) && stack.is(Weightless.PROJECTILE_WEAPONS)) {
            player.stopUsingItem();
            return false;
        }
        return true;
    }
}
