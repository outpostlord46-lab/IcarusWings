package net.frog.items;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;


public class IcarusWingsItem extends Item {
    public IcarusWingsItem(Properties settings) {
        super(settings);
    }

    public void inventoryTick(ItemStack stack, Level world, Entity entity, int slot, boolean selected) {
        if (!(world instanceof ServerLevel)) return;
        if (!(entity instanceof LivingEntity livingEntity)) return;

        // Only check if equipped in chest slot
        if (livingEntity.getItemBySlot(EquipmentSlot.CHEST) != stack) return;

        // Break the item at max durability
        if (stack.getDamageValue() >= stack.getMaxDamage() - 1) {
            stack.hurtAndBreak(1, livingEntity, EquipmentSlot.CHEST);
        }
    }
}