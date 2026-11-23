package net.frog;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.server.ServerAdvancementManager;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.phys.Vec3;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static net.frog.WingUtil.damageWings;
import static net.frog.WingUtil.safeDamage;

public class IcarusWingsServerTick {

    private static final Map<UUID, Long> lastFlapTime = new HashMap<>();
    private static final int WEAR_INTERVAL_TICKS = 20;
    /** Was 1/750 per tick; same average (~37.5 s) when rolled once per second. */
    private static final float LIGHTNING_CHANCE_PER_SECOND = 20f / 750f;

    public static void register() {
        PayloadTypeRegistry.serverboundPlay().register(FlapPayload.ID, FlapPayload.CODEC);
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> lastFlapTime.remove(handler.player.getUUID()));

        ServerPlayNetworking.registerGlobalReceiver(FlapPayload.ID, (payload, context) -> {
            ServerPlayer player = context.player();
            // Fabric already runs play-payload handlers on the server thread.
            //wings check
            ItemStack chestStack = player.getItemBySlot(EquipmentSlot.CHEST);
            boolean isIcarus = chestStack.is(ModItems.ICARUS_WINGS);
            boolean isMechanical = chestStack.is(ModItems.MECHANICAL_WINGS);
            Inventory inv = player.getInventory();
            boolean hasCoal = WingUtil.hasFuel(inv);

            if (!isIcarus && !(isMechanical && hasCoal)) return;
            if (!player.isFallFlying()) return;

            long currentTick = player.tickCount;
            long lastFlap = lastFlapTime.getOrDefault(player.getUUID(), 0L);

            if (currentTick < lastFlap) {
                lastFlapTime.put(player.getUUID(), 0L);
                lastFlap = 0L;
            }

            if (currentTick - lastFlap < WingUtil.FLAP_COOLDOWN_TICKS) return;
            lastFlapTime.put(player.getUUID(), currentTick);

            Vec3 vel = player.getDeltaMovement();
            player.setDeltaMovement(vel.x, Math.min(vel.y + 1, 1.2), vel.z);
            player.connection.send(new ClientboundSetEntityMotionPacket(player));

            if (isMechanical) {
                if (!WingUtil.consumeFuel(inv)) return;
                safeDamage(chestStack, 4, player);
            }

            if (isIcarus) {
                player.causeFoodExhaustion(1.0f);
                damageWings(chestStack, 6, player);
            }

        });

        ServerTickEvents.END_SERVER_TICK.register(server -> {
            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                ItemStack chestStack = player.getItemBySlot(EquipmentSlot.CHEST);

                boolean isIcarus = chestStack.is(ModItems.ICARUS_WINGS);
                boolean isMechanical = chestStack.is(ModItems.MECHANICAL_WINGS);
                if (!isIcarus && !isMechanical) continue;

                // Environmental wear is applied once per second (staggered per player), not every tick.
                if (player.tickCount % WEAR_INTERVAL_TICKS != 0) continue;

                Level world = player.level();
                boolean gliding = player.isFallFlying();
                boolean inEnd = world.dimension().equals(Level.END);
                boolean isDay = world.isBrightOutside();
                boolean inNether = world.dimension().equals(Level.NETHER);
                boolean inWater = player.isInWater();
                boolean inRain = !inWater && world.isRaining() && player.isInWaterOrRain();
                boolean isonFire = player.isOnFire();
                boolean isThundering = world.isThundering();

                //MECHANICAL WINGS
                if (isMechanical) {

                    // always: fire and Nether heat
                    if (isonFire) {
                        safeDamage(chestStack, 3, player);
                    }

                    if (inNether) {
                        safeDamage(chestStack, 2, player);
                    }

                    // while gliding: too high (day only, > y=250)
                    if (gliding && player.getY() > 350 && isDay && !inEnd) {
                        safeDamage(chestStack, 5, player);
                    }

                    // while gliding in a thunderstorm: lightning strike, ~once every 37.5 s on average
                    if (gliding && isThundering && inRain && player.getRandom().nextFloat() < LIGHTNING_CHANCE_PER_SECOND) {
                        LightningBolt lightning = EntityTypes.LIGHTNING_BOLT.create(world, EntitySpawnReason.TRIGGERED);
                        if (lightning != null) {
                            lightning.snapTo(player.getX(), player.getY(), player.getZ());
                            world.addFreshEntity(lightning);
                            safeDamage(chestStack, 30, player);

                            ServerAdvancementManager loader = server.getAdvancements();
                            AdvancementHolder advancement = loader.get(IcarusWings.id("struck_by_innovation"));
                            if (advancement != null) {
                                player.getAdvancements().award(advancement, "struck");
                            }
                        }
                    }
                }

                //ICARUS WINGS
                if (isIcarus) {
                    // while gliding: too high — melt (day only, > y=150)
                    if (gliding && player.getY() > 150 && isDay && !inEnd) {
                        damageWings(chestStack, 5, player);

                        if (chestStack.isEmpty()) {
                            ServerAdvancementManager loader = server.getAdvancements();
                            AdvancementHolder advancement = loader.get(IcarusWings.id("too_close_to_the_sun"));
                            if (advancement != null) {
                                player.getAdvancements().award(advancement, "wings_melted");
                            }
                        }
                    }

                    // always: nether heat, fire, water
                    if (inNether) {
                        damageWings(chestStack, 2, player);
                    }

                    if (isonFire) {
                        damageWings(chestStack, 3, player);
                    }

                    if (inWater) {
                        damageWings(chestStack, 20, player);
                    }

                    // while gliding: desert sun (day) and rain
                    if (gliding && isDay && world.getBiome(player.blockPosition()).is(Biomes.DESERT)) {
                        damageWings(chestStack, 2, player);
                    }

                    if (gliding && inRain) {
                        damageWings(chestStack, 3, player);
                    }
                }
            }
        });
    }
}
