package net.frog.mixin;

import net.frog.ModItems;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.FireworkRocketItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.ActionResult;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FireworkRocketItem.class)
public abstract class noFireworkMixin {

    @Inject(method = "use", at = @At("HEAD"), cancellable = true)
    private void disableFireworkBoost(World world, PlayerEntity user, Hand hand,
                                      CallbackInfoReturnable<ActionResult> cir) {

        ItemStack chest = user.getEquippedStack(EquipmentSlot.CHEST);

        // Check if the player is wearing the Mechanical Wings or Icarus Wings
        if (chest.isOf(ModItems.MECHANICAL_WINGS) || chest.isOf(ModItems.ICARUS_WINGS)) {

            // If they are cancel the firework
            cir.setReturnValue(ActionResult.PASS);
        }
    }
}
