package net.frog;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;

public class IcarusWingsClient implements ClientModInitializer {
	private boolean wasJumpPressed = false;
	private long lastFlapClientTick = -100;
	private static final int FLAP_COOLDOWN_TICKS = 10;

	@Override
	public void onInitializeClient() {
		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			if (client.player == null) return;

			boolean jumpPressed = client.options.jumpKey.isPressed();

			if (jumpPressed && !wasJumpPressed && client.player.isGliding()) {
				long currentTick = client.player.age;
				if (currentTick - lastFlapClientTick >= FLAP_COOLDOWN_TICKS) {
					ClientPlayNetworking.send(new FlapPayload());
					lastFlapClientTick = currentTick;
				}
			}
			wasJumpPressed = jumpPressed;
		});

		HudRenderCallback.EVENT.register((drawContext, tickCounter) -> {
			var client = net.minecraft.client.MinecraftClient.getInstance();
			if (client.player == null) return;
			if (!client.player.getEquippedStack(
							net.minecraft.entity.EquipmentSlot.CHEST)
					.isOf(ModItems.ICARUS_WINGS)) return;

			long ticksSinceFlap = client.player.age - lastFlapClientTick;

			if (ticksSinceFlap < 0) {
				lastFlapClientTick = client.player.age;
				ticksSinceFlap = 0;
			}

			float progress = Math.min((float) ticksSinceFlap / FLAP_COOLDOWN_TICKS, 1.0f);

			int screenWidth = client.getWindow().getScaledWidth();
			int screenHeight = client.getWindow().getScaledHeight();

			int barWidth = 50;
			int barHeight = 4;
			int x = screenWidth / 2 - barWidth / 2;
			int y = screenHeight - 52; // just above the hotbar

			// Dark background
			drawContext.fill(x - 1, y - 1, x + barWidth + 1, y + barHeight + 1, 0x80000000);
			// Colored fill — orange when charging, white when ready
			int color = progress >= 1.0f ? 0xFFFFFFFF : 0xFFFF8800;
			drawContext.fill(x, y, x + (int)(barWidth * progress), y + barHeight, color);
		});
	}
}