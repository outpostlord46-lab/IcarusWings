package net.frog;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/** Shared wing logic used by both the server handlers and the client HUD. */
public final class WingUtil {
    /** Ticks between flaps; shared by the client (input/HUD) and the server (validation). */
    public static final int FLAP_COOLDOWN_TICKS = 80;

    private WingUtil() {}

    private static boolean isFuel(ItemStack stack) {
        return stack.is(Items.COAL) || stack.is(Items.CHARCOAL);
    }

    /** True if the inventory holds any coal or charcoal. */
    public static boolean hasFuel(Inventory inv) {
        for (int i = 0; i < inv.getContainerSize(); i++) {
            if (isFuel(inv.getItem(i))) return true;
        }
        return false;
    }

    /** Removes one coal or charcoal from the first matching slot; false if there was none. */
    public static boolean consumeFuel(Inventory inv) {
        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack stack = inv.getItem(i);
            if (isFuel(stack)) {
                stack.shrink(1);
                if (stack.isEmpty()) {
                    inv.setItem(i, ItemStack.EMPTY);
                }
                return true;
            }
        }
        return false;
    }

    /** Damages chest-slot wings; they can break. */
    public static void damageWings(ItemStack stack, int amount, ServerPlayer player) {
        stack.hurtAndBreak(amount, player.level(), player, item -> player.onEquippedItemBroken(item, EquipmentSlot.CHEST));
    }

    /** Damages chest-slot wings but always leaves at least 1 durability. */
    public static void safeDamage(ItemStack stack, int amount, Player player) {
        int maxAllowedDamage = stack.getMaxDamage() - stack.getDamageValue() - 1;
        if (maxAllowedDamage > 0) {
            stack.hurtAndBreak(Math.min(amount, maxAllowedDamage), player, EquipmentSlot.CHEST);
        }
    }
}
