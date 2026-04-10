package net.frog.items;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.World;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;


public class IcarusWingsItem extends Item {
    public IcarusWingsItem(Settings settings) {
        super(settings);
    }

    public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected) {
        if (!(world instanceof ServerWorld)) return;
        if (!(entity instanceof LivingEntity livingEntity)) return;

        // Only check if equipped in chest slot
        if (livingEntity.getEquippedStack(EquipmentSlot.CHEST) != stack) return;

        // Break the item at max durability
        if (stack.getDamage() >= stack.getMaxDamage() - 1) {
            stack.damage(1, livingEntity, EquipmentSlot.CHEST);
        }
    }
}