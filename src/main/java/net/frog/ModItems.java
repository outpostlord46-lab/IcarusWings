package net.frog;

import net.frog.items.IcarusWingsItem;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.EquippableComponent;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.Item;
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
                    .maxDamage(400)
                    .component(DataComponentTypes.EQUIPPABLE,
                            EquippableComponent.builder(EquipmentSlot.CHEST)
                                    .equipSound(SoundEvents.ITEM_ARMOR_EQUIP_ELYTRA)
                                    .model(ICARUS_WINGS_EQUIPMENT)
                                    .build())
                    .component(DataComponentTypes.GLIDER, Unit.INSTANCE))
    );

    public static void registerItems() {}
}