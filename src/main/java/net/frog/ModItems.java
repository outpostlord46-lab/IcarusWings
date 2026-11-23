package net.frog;

import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Unit;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.equipment.EquipmentAsset;
import net.minecraft.world.item.equipment.EquipmentAssets;
import net.minecraft.world.item.equipment.Equippable;

public class ModItems {

    /** Fall damage multiplier while worn: x0.5 for Icarus Wings, x1.25 for Mechanical Wings. */
    private static ItemAttributeModifiers fallDamage(String name, double change) {
        return ItemAttributeModifiers.builder()
                .add(Attributes.FALL_DAMAGE_MULTIPLIER,
                        new AttributeModifier(IcarusWings.id(name + "_fall_damage"), change, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL),
                        EquipmentSlotGroup.CHEST)
                .build();
    }

    //ICARUS WINGS
    public static final ResourceKey<Item> ICARUS_WINGS_KEY = ResourceKey.create(
            Registries.ITEM,
            IcarusWings.id("icarus_wings")
    );

    public static final ResourceKey<EquipmentAsset> ICARUS_WINGS_EQUIPMENT = ResourceKey.create(
            EquipmentAssets.ROOT_ID,
            IcarusWings.id("icarus_wings")
    );

    public static final Item ICARUS_WINGS = Registry.register(
            BuiltInRegistries.ITEM,
            ICARUS_WINGS_KEY,
            new Item(new Item.Properties()
                    .setId(ICARUS_WINGS_KEY)
                    .durability(320)
                    .attributes(fallDamage("icarus_wings", -0.5))
                    .repairable(Items.HONEYCOMB)
                    .component(DataComponents.EQUIPPABLE,
                            Equippable.builder(EquipmentSlot.CHEST)
                                    .setEquipSound(SoundEvents.ARMOR_EQUIP_ELYTRA)
                                    .setAsset(ICARUS_WINGS_EQUIPMENT)
                                    .build())
                    .component(DataComponents.GLIDER, Unit.INSTANCE))
    );

    //MECHANICAL WINGS
    public static final ResourceKey<Item> MECHANICAL_WINGS_KEY = ResourceKey.create(
            Registries.ITEM,
            IcarusWings.id("mechanical_wings")
    );

    public static final ResourceKey<EquipmentAsset> MECHANICAL_WINGS_EQUIPMENT = ResourceKey.create(
            EquipmentAssets.ROOT_ID,
            IcarusWings.id("mechanical_wings")
    );

    public static final Item MECHANICAL_WINGS = Registry.register(
            BuiltInRegistries.ITEM,
            MECHANICAL_WINGS_KEY,
            new Item(new Item.Properties()
                    .setId(MECHANICAL_WINGS_KEY)
                    .durability(400)
                    .attributes(fallDamage("mechanical_wings", 0.25))
                    .rarity(Rarity.RARE)
                    .repairable(Items.IRON_INGOT)
                    .component(DataComponents.EQUIPPABLE,
                            Equippable.builder(EquipmentSlot.CHEST)
                                    .setEquipSound(SoundEvents.ARMOR_EQUIP_CHAIN)
                                    .setAsset(MECHANICAL_WINGS_EQUIPMENT)
                                    .build())
                    .component(DataComponents.GLIDER, Unit.INSTANCE))
    );

    private static final ResourceKey<CreativeModeTab> TOOLS_AND_UTILITIES =
            ResourceKey.create(Registries.CREATIVE_MODE_TAB, Identifier.withDefaultNamespace("tools_and_utilities"));

    public static void registerItems() {
        CreativeModeTabEvents.modifyOutputEvent(TOOLS_AND_UTILITIES)
                .register(output -> output.insertAfter(Items.ELYTRA, ICARUS_WINGS, MECHANICAL_WINGS));
    }
}