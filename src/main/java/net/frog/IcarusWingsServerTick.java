package net.frog;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.advancement.AdvancementEntry;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LightningEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;
import net.minecraft.server.ServerAdvancementLoader;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeKeys;
import net.minecraft.entity.SpawnReason;
import net.minecraft.server.world.ServerWorld;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class IcarusWingsServerTick {

    private static final Map<UUID, Long> lastFlapTime = new HashMap<>();
    private static final int FLAP_COOLDOWN_TICKS = 80;
    private static boolean consumeCoal(PlayerInventory inv) {
        for (int i = 0; i < inv.size(); i++) {
            ItemStack stack = inv.getStack(i);

            if (stack.isOf(Items.COAL) || stack.isOf(Items.CHARCOAL)) {
                //cost per flap
                stack.decrement(1);

                if (stack.isEmpty()) {
                    inv.setStack(i, ItemStack.EMPTY);
                }

                return true;
            }
        }

        return false;
    }
    private static void safeDamage(ItemStack stack, int amount, PlayerEntity player) {
        int remaining = stack.getMaxDamage() - stack.getDamage();

        // leave 1 durability
        int maxAllowedDamage = remaining - 1;

        if (maxAllowedDamage > 0) {
            stack.damage(Math.min(amount, maxAllowedDamage), player, EquipmentSlot.CHEST);
        }
    }

    public static void register() {
        PayloadTypeRegistry.playC2S().register(FlapPayload.ID, FlapPayload.CODEC);

        ServerPlayNetworking.registerGlobalReceiver(FlapPayload.ID, (payload, context) -> {
            ServerPlayerEntity player = context.player();
            context.server().execute(() -> {
                //wings check
                ItemStack chestStack = player.getEquippedStack(EquipmentSlot.CHEST);
                boolean isIcarus = chestStack.isOf(ModItems.ICARUS_WINGS);
                boolean isMechanical = chestStack.isOf(ModItems.MECHANICAL_WINGS);
                //coal check
                boolean hasCoal = false;
                PlayerInventory inv = player.getInventory();
                for (int i = 0; i < inv.size(); i++) {
                    ItemStack stack = inv.getStack(i);
                    if (stack.isOf(Items.COAL) || stack.isOf(Items.CHARCOAL)) {
                        hasCoal = true;
                        break;
                    }
                }

                if (!isIcarus && !(isMechanical && hasCoal)) return;
                if (!player.isGliding()) return;

                long currentTick = player.age;
                long lastFlap = lastFlapTime.getOrDefault(player.getUuid(), 0L);

                if (currentTick < lastFlap) {
                    lastFlapTime.put(player.getUuid(), 0L);
                    lastFlap = 0L;
                }

                if (currentTick - lastFlap < FLAP_COOLDOWN_TICKS) return;
                lastFlapTime.put(player.getUuid(), currentTick);

                Vec3d vel = player.getVelocity();
                player.setVelocity(vel.x, Math.min(vel.y + 1, 1.2), vel.z);
                player.networkHandler.sendPacket(new EntityVelocityUpdateS2CPacket(player));

                if (isMechanical) {
                    if (!consumeCoal(inv)) return;
                    safeDamage(chestStack, 5, player);
                }

                if (isIcarus) {
                    player.addExhaustion(1.0f);
                    chestStack.damage(
                            10, player.getEntityWorld(), player, item -> player.sendEquipmentBreakStatus(item, EquipmentSlot.CHEST)
                    );
                }

            });
        });

        ServerTickEvents.END_SERVER_TICK.register(server -> {
            for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                ItemStack chestStack = player.getEquippedStack(EquipmentSlot.CHEST);

                boolean isIcarus = chestStack.isOf(ModItems.ICARUS_WINGS);
                boolean isMechanical = chestStack.isOf(ModItems.MECHANICAL_WINGS);
                if (!isIcarus && !isMechanical) continue;

                World world = player.getEntityWorld();
                boolean inEnd = world.getRegistryKey().equals(World.END);
                boolean isDay = world.isDay();
                boolean inNether = world.getRegistryKey().equals(World.NETHER);
                boolean inDesert = world.getBiome(player.getBlockPos()).matchesKey(BiomeKeys.DESERT);
                boolean inWater = player.isTouchingWater();
                boolean inRain = !inWater && world.isRaining() && player.isTouchingWaterOrRain();
                boolean isonFire = player.isOnFire();
                boolean isThundering = world.isThundering();

                //MECHANICAL WINGS
                if (isMechanical) {
                    // water damage
                    if (inWater) {
                        safeDamage(chestStack, 20, player);
                    }

                    if (isonFire) {
                        safeDamage(chestStack, 3, player);
                    }

                    // too high — damage (day only, > y=250)
                    if (player.getY() > 350 && isDay && !inEnd) {
                        safeDamage(chestStack, 5, player);
                    }

                    if (inNether) {
                        chestStack.damage(
                                2, player.getEntityWorld(), player, item -> player.sendEquipmentBreakStatus(item, EquipmentSlot.CHEST)
                        );
                    }

                    // lightning strike (wings in a storm)
                    // 1 in 750 chance per tick (~every 40 seconds)
                    if (isThundering && inRain && player.getRandom().nextInt(750) == 0) {
                        LightningEntity lightning = EntityType.LIGHTNING_BOLT.create(world, SpawnReason.TRIGGERED);
                        if (lightning != null) {
                            lightning.refreshPositionAfterTeleport(player.getX(), player.getY(), player.getZ());
                            world.spawnEntity(lightning);
                            safeDamage(chestStack, 30, player);
                        }
                        ServerAdvancementLoader loader = server.getAdvancementLoader();
                        AdvancementEntry advancement = loader.get(
                                Identifier.of("icarus-wings", "struck_by_innovation"));
                        if (advancement != null) {
                            player.getAdvancementTracker().grantCriterion(advancement, "struck");
                        }
                    }
                }

                //ICARUS WINGS
                if (isIcarus) {
                    // too high — melt (day only, > y=150)
                    if (player.getY() > 150 && isDay && !inEnd) {
                        chestStack.damage(
                                5, player.getEntityWorld(), player, item -> player.sendEquipmentBreakStatus(item, EquipmentSlot.CHEST)
                        );

                        if (chestStack.isEmpty()) {
                            ServerAdvancementLoader loader = server.getAdvancementLoader();
                            AdvancementEntry advancement = loader.get(
                                    Identifier.of("icarus-wings", "too_close_to_the_sun"));
                            if (advancement != null) {
                                player.getAdvancementTracker().grantCriterion(advancement, "wings_melted");
                            }
                        }
                    }

                    // nether — melts from heat
                    if (inNether) {
                        chestStack.damage(
                                2, player.getEntityWorld(), player, item -> player.sendEquipmentBreakStatus(item, EquipmentSlot.CHEST)
                        );
                    }

                    // desert — melts slowly in the sun
                    if (inDesert && isDay) {
                        chestStack.damage(
                                2, player.getEntityWorld(), player, item -> player.sendEquipmentBreakStatus(item, EquipmentSlot.CHEST)
                        );
                    }

                    // rain — damages wings
                    if (inRain) {
                        chestStack.damage(
                                3, player.getEntityWorld(), player, item -> player.sendEquipmentBreakStatus(item, EquipmentSlot.CHEST)
                        );
                    }

                    // fire burns
                    if (isonFire) {
                        chestStack.damage(
                                3, player.getEntityWorld(), player, item -> player.sendEquipmentBreakStatus(item, EquipmentSlot.CHEST)
                        );
                    }

                    // water — shatters wings fast
                    if (inWater) {
                        chestStack.damage(
                                20, player.getEntityWorld(), player, item -> player.sendEquipmentBreakStatus(item, EquipmentSlot.CHEST)
                        );
                    }
                }
            }
        });
    }
}