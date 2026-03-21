package net.frog;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.advancement.AdvancementEntry;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;
import net.minecraft.server.ServerAdvancementLoader;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeKeys;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class IcarusWingsServerTick {

    private static final Map<UUID, Long> lastFlapTime = new HashMap<>();
    private static final int FLAP_COOLDOWN_TICKS = 20;

    public static void register() {
        PayloadTypeRegistry.playC2S().register(FlapPayload.ID, FlapPayload.CODEC);

        ServerPlayNetworking.registerGlobalReceiver(FlapPayload.ID, (payload, context) -> {
            ServerPlayerEntity player = context.player();
            context.server().execute(() -> {
                if (!player.getEquippedStack(EquipmentSlot.CHEST).isOf(ModItems.ICARUS_WINGS)) return;
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
            });
        });

        ServerTickEvents.END_SERVER_TICK.register(server -> {
            for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                boolean wearingWings = player.getEquippedStack(EquipmentSlot.CHEST)
                        .isOf(ModItems.ICARUS_WINGS);

                if (!wearingWings) continue;

                World world = player.getEntityWorld();
                boolean inEnd = world.getRegistryKey().equals(World.END);
                boolean isDay = world.isDay();
                boolean inNether = world.getRegistryKey().equals(World.NETHER);
                boolean inDesert = world.getBiome(player.getBlockPos()).matchesKey(BiomeKeys.DESERT);
                boolean inWater = player.isTouchingWater();
                boolean inRain = !inWater && world.isRaining() && player.isTouchingWaterOrRain();
                boolean isonFire = player.isOnFire();

                // Too high — melt (day only)
                if (player.getY() > 150 && isDay && !inEnd) {
                    ItemStack wings = player.getEquippedStack(EquipmentSlot.CHEST);
                    wings.damage(5, player, EquipmentSlot.CHEST);

                    if (wings.isEmpty()) {
                        ServerAdvancementLoader loader = server.getAdvancementLoader();
                        AdvancementEntry advancement = loader.get(
                                Identifier.of("icarus-wings", "too_close_to_the_sun"));
                        if (advancement != null) {
                            player.getAdvancementTracker().grantCriterion(advancement, "wings_melted");
                        }
                    }
                }

                // Nether — melts from heat
                if (inNether) {
                    player.getEquippedStack(EquipmentSlot.CHEST)
                            .damage(5, player, EquipmentSlot.CHEST);
                }

                // Desert — melts slowly in the sun
                if (inDesert && isDay) {
                    player.getEquippedStack(EquipmentSlot.CHEST)
                            .damage(2, player, EquipmentSlot.CHEST);
                }

                // Rain — damages wings
                if (inRain) {
                    player.getEquippedStack(EquipmentSlot.CHEST)
                            .damage(3, player, EquipmentSlot.CHEST);
                }

                // Fire burns
                if (isonFire) {
                    player.getEquippedStack(EquipmentSlot.CHEST)
                            .damage(3, player, EquipmentSlot.CHEST);
                }

                // Water — shatters wings fast
                if (inWater) {
                    player.getEquippedStack(EquipmentSlot.CHEST)
                            .damage(20, player, EquipmentSlot.CHEST);
                }
            }
        });
    }
}