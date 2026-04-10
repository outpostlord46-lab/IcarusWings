package net.frog.mixin;

import net.frog.ModItems;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class movementSpeedMixin {

    @Inject(method = "getMovementSpeed", at = @At("RETURN"), cancellable = true)
    private void applyMechanicalWeightPenalty(CallbackInfoReturnable<Float> cir) {
        LivingEntity entity = (LivingEntity) (Object) this;

        // slow them down if they are walking on the ground
        if (entity.isOnGround()) {
            ItemStack chestStack = entity.getEquippedStack(EquipmentSlot.CHEST);

            if (chestStack.isOf(ModItems.MECHANICAL_WINGS)) {
                float originalSpeed = cir.getReturnValue();

                //Reduce speed by 15% (multiply by 0.85)
                //adjust this number to make it feel heavier or lighter
                cir.setReturnValue(originalSpeed * 0.85f);
            }
        }
    }
}
