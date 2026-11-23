package net.frog.mixin;

import net.frog.IcarusWings;
import net.frog.ModItems;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Grants "Second Chance Engineering" when repaired Mechanical Wings are taken out of an anvil. */
@Mixin(AnvilMenu.class)
public abstract class AnvilRepairMixin {

    @Inject(method = "onTake", at = @At("HEAD"))
    private void icarusWings$awardRepair(Player player, ItemStack result, CallbackInfo ci) {
        if (!(player instanceof ServerPlayer serverPlayer)) return;
        if (!result.is(ModItems.MECHANICAL_WINGS)) return;

        // slot 0 is the left anvil input; it still holds the original stack at HEAD
        ItemStack input = ((AbstractContainerMenu) (Object) this).getSlot(0).getItem();
        if (!input.is(ModItems.MECHANICAL_WINGS) || result.getDamageValue() >= input.getDamageValue()) return;

        AdvancementHolder advancement = serverPlayer.level().getServer().getAdvancements()
                .get(IcarusWings.id("second_chance_engineering"));
        if (advancement != null) {
            serverPlayer.getAdvancements().award(advancement, "repair_event");
        }
    }
}
