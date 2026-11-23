package net.frog;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.server.ServerAdvancementManager;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.phys.Vec3;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class IcarusWingsServerTick {

    private static final Map<UUID, Long> lastFlapTime = new HashMap<>();
    private static final int FLAP_COOLDOWN_TICKS = 80;
    private static boolean consumeCoal(Inventory inv) {
        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack stack = inv.getItem(i);

            if (stack.is(Items.COAL) || stack.is(Items.CHARCOAL)) {
                //cost per flap
                stack.shrink(1);

                if (stack.isEmpty()) {
                    inv.setItem(i, ItemStack.EMPTY);
                }

                return true;
            }
        }

        return false;
    }
    private static void safeDamage(ItemStack stack, int amount, Player player) {
        int remaining = stack.getMaxDamage() - stack.getDamageValue();

        // leave 1 durability
        int maxAllowedDamage = remaining - 1;

        if (maxAllowedDamage > 0) {
            stack.hurtAndBreak(Math.min(amount, maxAllowedDamage), player, EquipmentSlot.CHEST);
        }
    }

    public static void register() {
        PayloadTypeRegistry.playC2S().register(FlapPayload.ID, FlapPayload.CODEC);

        ServerPlayNetworking.registerGlobalReceiver(FlapPayload.ID, (payload, context) -> {
            ServerPlayer player = context.player();
            context.server().execute(() -> {
                //wings check
                ItemStack chestStack = player.getItemBySlot(EquipmentSlot.CHEST);
                boolean isIcarus = chestStack.is(ModItems.ICARUS_WINGS);
                boolean isMechanical = chestStack.is(ModItems.MECHANICAL_WINGS);
                //coal check
                boolean hasCoal = false;
                Inventory inv = player.getInventory();
                for (int i = 0; i < inv.getContainerSize(); i++) {
                    ItemStack stack = inv.getItem(i);
                    if (stack.is(Items.COAL) || stack.is(Items.CHARCOAL)) {
                        hasCoal = true;
                        break;
                    }
                }

                if (!isIcarus && !(isMechanical && hasCoal)) return;
                if (!player.isFallFlying()) return;

                long currentTick = player.tickCount;
                long lastFlap = lastFlapTime.getOrDefault(player.getUUID(), 0L);

                if (currentTick < lastFlap) {
                    lastFlapTime.put(player.getUUID(), 0L);
                    lastFlap = 0L;
                }

                if (currentTick - lastFlap < FLAP_COOLDOWN_TICKS) return;
                lastFlapTime.put(player.getUUID(), currentTick);

                Vec3 vel = player.getDeltaMovement();
                player.setDeltaMovement(vel.x, Math.min(vel.y + 1, 1.2), vel.z);
                player.connection.send(new ClientboundSetEntityMotionPacket(player));

                if (isMechanical) {
                    if (!consumeCoal(inv)) return;
                    safeDamage(chestStack, 4, player);
                }

                if (isIcarus) {
                    player.causeFoodExhaustion(1.0f);
                    chestStack.hurtAndBreak(
                            6, player.level(), player, item -> player.onEquippedItemBroken(item, EquipmentSlot.CHEST)
                    );
                }

            });
        });

        ServerTickEvents.END_SERVER_TICK.register(server -> {
            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                ItemStack chestStack = player.getItemBySlot(EquipmentSlot.CHEST);

                boolean isIcarus = chestStack.is(ModItems.ICARUS_WINGS);
                boolean isMechanical = chestStack.is(ModItems.MECHANICAL_WINGS);
                if (!isIcarus && !isMechanical) continue;

                Level world = player.level();
                boolean inEnd = world.dimension().equals(Level.END);
                boolean isDay = world.isBrightOutside();
                boolean inNether = world.dimension().equals(Level.NETHER);
                boolean inDesert = world.getBiome(player.blockPosition()).is(Biomes.DESERT);
                boolean inWater = player.isInWater();
                boolean inRain = !inWater && world.isRaining() && player.isInWaterOrRain();
                boolean isonFire = player.isOnFire();
                boolean isThundering = world.isThundering();

                //MECHANICAL WINGS
                if (isMechanical) {

                    if (isonFire) {
                        safeDamage(chestStack, 3, player);
                    }

                    // too high — damage (day only, > y=250)
                    if (player.getY() > 350 && isDay && !inEnd) {
                        safeDamage(chestStack, 5, player);
                    }

                    if (inNether) {
                        chestStack.hurtAndBreak(
                                2, player.level(), player, item -> player.onEquippedItemBroken(item, EquipmentSlot.CHEST)
                        );
                    }

                    // lightning strike (wings in a storm)
                    // 1 in 750 chance per tick (~every 40 seconds)
                    if (isThundering && inRain && player.getRandom().nextInt(750) == 0) {
                        LightningBolt lightning = EntityType.LIGHTNING_BOLT.create(world, EntitySpawnReason.TRIGGERED);
                        if (lightning != null) {
                            lightning.snapTo(player.getX(), player.getY(), player.getZ());
                            world.addFreshEntity(lightning);
                            safeDamage(chestStack, 30, player);
                        }
                        ServerAdvancementManager loader = server.getAdvancements();
                        AdvancementHolder advancement = loader.get(
                                Identifier.fromNamespaceAndPath("icarus-wings", "struck_by_innovation"));
                        if (advancement != null) {
                            player.getAdvancements().award(advancement, "struck");
                        }
                    }
                }

                //ICARUS WINGS
                if (isIcarus) {
                    // too high — melt (day only, > y=150)
                    if (player.getY() > 150 && isDay && !inEnd) {
                        chestStack.hurtAndBreak(
                                5, player.level(), player, item -> player.onEquippedItemBroken(item, EquipmentSlot.CHEST)
                        );

                        if (chestStack.isEmpty()) {
                            ServerAdvancementManager loader = server.getAdvancements();
                            AdvancementHolder advancement = loader.get(
                                    Identifier.fromNamespaceAndPath("icarus-wings", "too_close_to_the_sun"));
                            if (advancement != null) {
                                player.getAdvancements().award(advancement, "wings_melted");
                            }
                        }
                    }

                    // nether — melts from heat
                    if (inNether) {
                        chestStack.hurtAndBreak(
                                2, player.level(), player, item -> player.onEquippedItemBroken(item, EquipmentSlot.CHEST)
                        );
                    }

                    // desert — melts slowly in the sun
                    if (inDesert && isDay) {
                        chestStack.hurtAndBreak(
                                2, player.level(), player, item -> player.onEquippedItemBroken(item, EquipmentSlot.CHEST)
                        );
                    }

                    // rain — damages wings
                    if (inRain) {
                        chestStack.hurtAndBreak(
                                3, player.level(), player, item -> player.onEquippedItemBroken(item, EquipmentSlot.CHEST)
                        );
                    }

                    // fire burns
                    if (isonFire) {
                        chestStack.hurtAndBreak(
                                3, player.level(), player, item -> player.onEquippedItemBroken(item, EquipmentSlot.CHEST)
                        );
                    }

                    // water — shatters wings fast
                    if (inWater) {
                        chestStack.hurtAndBreak(
                                20, player.level(), player, item -> player.onEquippedItemBroken(item, EquipmentSlot.CHEST)
                        );
                    }
                }
            }
        });
    }
}