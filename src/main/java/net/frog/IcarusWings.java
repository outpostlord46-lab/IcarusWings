package net.frog;

import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.fabricmc.fabric.api.loot.v3.LootTableEvents;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraft.world.level.storage.loot.providers.number.ints.ContextIntProviders;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IcarusWings implements ModInitializer {
	public static final String MOD_ID = "icarus-wings";

	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	public static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(MOD_ID, path);
	}

	@Override
	public void onInitialize() {
		LOGGER.info("Icarus Wings initializing!");

		// Register items
		ModItems.registerItems();
		IcarusWingsServerTick.register();

		// Wings can't be boosted with firework rockets (runs on both client and server).
		UseItemCallback.EVENT.register((player, level, hand) -> {
			if (!player.getItemInHand(hand).is(Items.FIREWORK_ROCKET)) return InteractionResult.PASS;
			var chest = player.getItemBySlot(EquipmentSlot.CHEST);
			if (chest.is(ModItems.MECHANICAL_WINGS) || chest.is(ModItems.ICARUS_WINGS)) {
				return InteractionResult.FAIL;
			}
			return InteractionResult.PASS;
		});

		// Loot table injection
		LootTableEvents.MODIFY.register((key, tableBuilder, source, registryLookup) -> {
			if (BuiltInLootTables.SIMPLE_DUNGEON.equals(key) ||
					BuiltInLootTables.STRONGHOLD_LIBRARY.equals(key)) {
				tableBuilder.pool(LootPool.lootPool()
						.setRolls(ContextIntProviders.exactly(1))
						.add(LootItem.lootTableItem(ModItems.ICARUS_WINGS))
						.when(LootItemRandomChanceCondition.randomChance(0.12f))
						.build());
			}
		});
	}
}