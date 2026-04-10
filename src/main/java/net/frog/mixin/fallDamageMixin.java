package net.frog.mixin;

import net.frog.ModItems;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(LivingEntity.class)
public abstract class fallDamageMixin {

    @ModifyVariable(method = "handleFallDamage", at = @At("HEAD"), argsOnly = true)
    private float reduceMechanicalWingFallDamage(float fallDistance) {

        LivingEntity entity = (LivingEntity) (Object) this;


        ItemStack chestStack = entity.getEquippedStack(EquipmentSlot.CHEST);

        // if it's the Icarus Wings reduce the distance by 50%
        if (chestStack.isOf(ModItems.ICARUS_WINGS)) {
            //Effectively makes a 20-block fall a 10-block fall
            return fallDistance * 0.5f;
        }
        if (chestStack.isOf(ModItems.MECHANICAL_WINGS)) {
            //Effectively makes a 20-block fall a 25-block fall
            return fallDistance * 1.25f;
        }

        return fallDistance;
    }
}