package net.frog.mixin;

import net.frog.ModItems;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class MovementSpeedMixin {

    @Inject(method = "getSpeed", at = @At("RETURN"), cancellable = true)
    private void applyMechanicalWeightPenalty(CallbackInfoReturnable<Float> cir) {
        LivingEntity entity = (LivingEntity) (Object) this;

        // slow them down if they are walking on the ground
        if (entity.onGround()) {
            ItemStack chestStack = entity.getItemBySlot(EquipmentSlot.CHEST);

            if (chestStack.is(ModItems.MECHANICAL_WINGS)) {
                float originalSpeed = cir.getReturnValue();

                //Reduce speed by 15% (multiply by 0.85)
                //adjust this number to make it feel heavier or lighter
                cir.setReturnValue(originalSpeed * 0.85f);
            }
        }
    }
}
