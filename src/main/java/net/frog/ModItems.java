package net.frog;

import net.frog.items.IcarusWingsItem;
import net.frog.items.MechanicalWingsItem;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.EquippableComponent;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.item.equipment.EquipmentAsset;
import net.minecraft.item.equipment.EquipmentAssetKeys;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;
import net.minecraft.util.Unit;

public class ModItems {

    //ICARUS WINGS
    public static final RegistryKey<Item> ICARUS_WINGS_KEY = RegistryKey.of(
            RegistryKeys.ITEM,
            Identifier.of("icarus-wings", "icarus_wings")
    );

    public static final RegistryKey<EquipmentAsset> ICARUS_WINGS_EQUIPMENT = RegistryKey.of(
            EquipmentAssetKeys.REGISTRY_KEY,
            Identifier.of("icarus-wings", "icarus_wings")
    );

    public static final Item ICARUS_WINGS = Registry.register(
            Registries.ITEM,
            ICARUS_WINGS_KEY,
            new IcarusWingsItem(new Item.Settings()
                    .registryKey(ICARUS_WINGS_KEY)
                    .maxDamage(250)
                    .repairable(Items.HONEYCOMB)
                    .component(DataComponentTypes.EQUIPPABLE,
                            EquippableComponent.builder(EquipmentSlot.CHEST)
                                    .equipSound(SoundEvents.ITEM_ARMOR_EQUIP_ELYTRA)
                                    .model(ICARUS_WINGS_EQUIPMENT)
                                    .build())
                    .component(DataComponentTypes.GLIDER, Unit.INSTANCE))
    );

    //MECHANICAL WINGS
    public static final RegistryKey<Item> MECHANICAL_WINGS_KEY = RegistryKey.of(
            RegistryKeys.ITEM,
            Identifier.of("icarus-wings", "mechanical_wings")
    );

    public static final RegistryKey<EquipmentAsset> MECHANICAL_WINGS_EQUIPMENT = RegistryKey.of(
            EquipmentAssetKeys.REGISTRY_KEY,
            Identifier.of("icarus-wings", "mechanical_wings")
    );

    public static final Item MECHANICAL_WINGS = Registry.register(
            Registries.ITEM,
            MECHANICAL_WINGS_KEY,
            new MechanicalWingsItem(new Item.Settings()
                    .registryKey(MECHANICAL_WINGS_KEY)
                    .maxDamage(400)
                    .repairable(Items.IRON_INGOT)
                    .component(DataComponentTypes.EQUIPPABLE,
                            EquippableComponent.builder(EquipmentSlot.CHEST)
                                    .equipSound(SoundEvents.ITEM_ARMOR_EQUIP_CHAIN)
                                    .model(MECHANICAL_WINGS_EQUIPMENT)
                                    .build())
                    .component(DataComponentTypes.GLIDER, Unit.INSTANCE))
    );

    public static void registerItems() {}
}