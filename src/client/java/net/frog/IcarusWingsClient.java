package net.frog;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.Item;
import net.minecraft.util.Identifier;

public class IcarusWingsClient implements ClientModInitializer {
	private boolean wasJumpPressed = false;
	private long lastFlapClientTick = -100;
	private static final int FLAP_COOLDOWN_TICKS = 80;
    private static final Identifier SEMI_CIRCLE_TEXTURE = Identifier.of("icarus-wings", "textures/gui/semi_circle.png");

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

            PlayerInventory inv = client.player.getInventory();
            boolean hasCoal = false;
            for (int i = 0; i < inv.size(); i++) {
                ItemStack stack = inv.getStack(i);
                if (stack.isOf(Items.COAL) || stack.isOf(Items.CHARCOAL)) {
                    hasCoal = true;
                    break;
                }
            }

            var chestStack = client.player.getEquippedStack(net.minecraft.entity.EquipmentSlot.CHEST);
            boolean wearingIcarus = chestStack.isOf(ModItems.ICARUS_WINGS);
            boolean wearingMechanical = chestStack.isOf(ModItems.MECHANICAL_WINGS) && hasCoal;

            long ticksSinceFlap = client.player.age - lastFlapClientTick;
            float progress = Math.min((float) ticksSinceFlap / FLAP_COOLDOWN_TICKS, 1.0f);

            if (wearingIcarus || wearingMechanical) {
                int screenWidth = client.getWindow().getScaledWidth();
                int screenHeight = client.getWindow().getScaledHeight();

                int barWidth = 182;
                int x = screenWidth / 2 - 91;
                int y = screenHeight - 32 + 3;

                drawContext.drawGuiTexture(
                        RenderPipelines.GUI_TEXTURED,
                        net.minecraft.util.Identifier.ofVanilla("hud/jump_bar_background"),
                        x, y, barWidth, 5
                );

                int xpBarY = screenHeight - 32;
                int centerX = screenWidth / 2;
                int semiCircleWidth = 15;  // match the texture width
                int semiCircleHeight = 9;  // match the texture height
                int i = centerX - (semiCircleWidth / 2) - 1;
                int j = xpBarY - semiCircleHeight + 3; // adjustment above XP bar

                drawContext.drawTexture(
                    RenderPipelines.GUI_TEXTURED,
                    SEMI_CIRCLE_TEXTURE,
                    i, j,               // screen position
                    0f, 0f,             // u, v in texture
                    semiCircleWidth, semiCircleHeight, // width & height to draw
                    semiCircleWidth, semiCircleHeight  // texture width & height
                );

                if (progress > 0) {
                    int filledWidth = (int) (progress * barWidth);

                    drawContext.drawGuiTexture(
                            RenderPipelines.GUI_TEXTURED,
                            net.minecraft.util.Identifier.ofVanilla("hud/jump_bar_progress"),
                            182, 5,
                            0, 0,
                            x, y,
                            filledWidth, 5
                    );
                }
            }
        });
	}
}