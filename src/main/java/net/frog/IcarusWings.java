package net.frog;

import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.loot.v3.LootTableEvents;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
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
			if (BuiltInLootTables.SIMPLE_DUNGEON.equals(key) ||
					BuiltInLootTables.STRONGHOLD_LIBRARY.equals(key)) {
				tableBuilder.pool(LootPool.lootPool()
						.setRolls(ConstantValue.exactly(1))
						.add(LootItem.lootTableItem(ModItems.ICARUS_WINGS))
						.when(LootItemRandomChanceCondition.randomChance(0.12f))
						.build());
			}
		});
	}
}