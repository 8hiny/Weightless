package shiny.weightless.mixin;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import shiny.weightless.ModConfig;
import shiny.weightless.Weightless;
import shiny.weightless.common.component.WeightlessComponent;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin {

    @Inject(method = "use", at = @At(value = "HEAD"), cancellable = true)
    private void weightless$preventItemUse(World world, PlayerEntity user, Hand hand, CallbackInfoReturnable<TypedActionResult<ItemStack>> cir) {
        if (ModConfig.preventRangedWeapons) {
            ItemStack stack = user.getStackInHand(hand);
            if (WeightlessComponent.flying(user) && stack.isIn(Weightless.PROJECTILE_WEAPONS)) {
                cir.setReturnValue(TypedActionResult.fail(stack));
            }
        }
    }

    @WrapWithCondition(method = "usageTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/Item;usageTick(Lnet/minecraft/world/World;Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/item/ItemStack;I)V"))
    private boolean weightless$preventItemUsageTick(Item instance, World world, LivingEntity user, ItemStack stack, int remainingUseTicks) {
        if (ModConfig.preventRangedWeapons && user instanceof PlayerEntity player && WeightlessComponent.flying(player) && stack.isIn(Weightless.PROJECTILE_WEAPONS)) {
            user.clearActiveItem();
            return false;
        }
        return true;
    }
}
