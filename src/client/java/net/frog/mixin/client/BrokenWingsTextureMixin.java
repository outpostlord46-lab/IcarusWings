package net.frog.mixin.client;

import com.llamalad7.mixinextras.sugar.Local;
import net.frog.IcarusWings;
import net.frog.ModItems;
import net.minecraft.client.renderer.entity.layers.WingsLayer;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.equipment.EquipmentAsset;
import net.minecraft.world.item.equipment.EquipmentAssets;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

/**
 * Draws Mechanical Wings with the broken texture once they're at their last point of durability
 * (the same "broken" check the item icon uses). Render-only: the item itself is not changed.
 */
@Mixin(WingsLayer.class)
public abstract class BrokenWingsTextureMixin {

    @Unique
    private static final ResourceKey<EquipmentAsset> MECHANICAL_WINGS_BROKEN =
            ResourceKey.create(EquipmentAssets.ROOT_ID, IcarusWings.id("mechanical_wings_broken"));

    @ModifyArg(
            method = "submit(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;ILnet/minecraft/client/renderer/entity/state/HumanoidRenderState;FF)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/layers/EquipmentLayerRenderer;renderLayers(Lnet/minecraft/client/resources/model/EquipmentClientInfo$LayerType;Lnet/minecraft/resources/ResourceKey;Lnet/minecraft/client/model/Model;Ljava/lang/Object;Lnet/minecraft/world/item/ItemStack;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;ILnet/minecraft/resources/Identifier;II)V"),
            index = 1)
    private ResourceKey<EquipmentAsset> icarusWings$brokenTexture(ResourceKey<EquipmentAsset> asset,
                                                                  @Local(argsOnly = true) HumanoidRenderState state) {
        ItemStack chest = state.chestEquipment;
        if (chest.is(ModItems.MECHANICAL_WINGS) && chest.nextDamageWillBreak()) {
            return MECHANICAL_WINGS_BROKEN;
        }
        return asset;
    }
}
