package net.frog;

import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.loot.v3.LootTableEvents;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootPool;
import net.minecraft.loot.LootTables;
import net.minecraft.loot.condition.RandomChanceLootCondition;
import net.minecraft.loot.entry.ItemEntry;
import net.minecraft.loot.provider.number.ConstantLootNumberProvider;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.Vec3d;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IcarusWings implements ModInitializer {
	public static final String MOD_ID = "icarus-wings";

	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		LOGGER.info("Icarus Wings initializing!");

		// Register items
		ModItems.registerItems();
		IcarusWingsServerTick.register();

		// Loot table injection
		LootTableEvents.MODIFY.register((key, tableBuilder, source, registryLookup) -> {
			if (LootTables.SIMPLE_DUNGEON_CHEST.equals(key) ||
					LootTables.STRONGHOLD_LIBRARY_CHEST.equals(key)) {
				tableBuilder.pool(LootPool.builder()
						.rolls(ConstantLootNumberProvider.create(1))
						.with(ItemEntry.builder(ModItems.ICARUS_WINGS))
						.conditionally(RandomChanceLootCondition.builder(0.12f))
						.build());
			}
		});
	}
}