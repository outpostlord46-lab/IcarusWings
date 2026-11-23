package net.frog.mixin;

import net.frog.ModItems;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.FireworkRocketItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FireworkRocketItem.class)
public abstract class noFireworkMixin {

    @Inject(method = "use", at = @At("HEAD"), cancellable = true)
    private void disableFireworkBoost(Level world, Player user, InteractionHand hand,
                                      CallbackInfoReturnable<InteractionResult> cir) {

        ItemStack chest = user.getItemBySlot(EquipmentSlot.CHEST);

        // Check if the player is wearing the Mechanical Wings or Icarus Wings
        if (chest.is(ModItems.MECHANICAL_WINGS) || chest.is(ModItems.ICARUS_WINGS)) {

            // If they are cancel the firework
            cir.setReturnValue(InteractionResult.PASS);
        }
    }
}
