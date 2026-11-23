package net.frog;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class IcarusWingsClient implements ClientModInitializer {
	private boolean wasJumpPressed = false;
	private long lastFlapClientTick = -100;
	private static final int FLAP_COOLDOWN_TICKS = 80;
    private static final Identifier FLAP_BAR_ID = Identifier.fromNamespaceAndPath("icarus-wings", "flap_cooldown_bar");
    private boolean hudErrorLogged = false;
    private static final Identifier SEMI_CIRCLE_TEXTURE = Identifier.fromNamespaceAndPath("icarus-wings", "textures/gui/semi_circle.png");

	@Override
	public void onInitializeClient() {
		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			if (client.player == null) return;

			boolean jumpPressed = client.options.keyJump.isDown();

			if (jumpPressed && !wasJumpPressed && client.player.isFallFlying()) {
				long currentTick = client.player.tickCount;
				if (currentTick - lastFlapClientTick >= FLAP_COOLDOWN_TICKS) {
					ClientPlayNetworking.send(new FlapPayload());
					lastFlapClientTick = currentTick;
				}
			}
			wasJumpPressed = jumpPressed;
		});

        // 26.2+: HudRenderCallback was removed; HUD is retained-mode via HudElementRegistry.
        // addLast keeps the old draw order (HudRenderCallback ran after the vanilla HUD).
        HudElementRegistry.addLast(FLAP_BAR_ID, (graphics, deltaTracker) -> {
            try {
                extractFlapBar(graphics);
            } catch (Throwable t) {
                if (!hudErrorLogged) {
                    hudErrorLogged = true;
                    IcarusWings.LOGGER.error("Icarus Wings flap bar failed to render; disabling further error logs", t);
                }
            }
        });
	}

    private void extractFlapBar(GuiGraphicsExtractor drawContext) {
            var client = Minecraft.getInstance();
            if (client.player == null) return;

            Inventory inv = client.player.getInventory();
            boolean hasCoal = false;
            for (int i = 0; i < inv.getContainerSize(); i++) {
                ItemStack stack = inv.getItem(i);
                if (stack.is(Items.COAL) || stack.is(Items.CHARCOAL)) {
                    hasCoal = true;
                    break;
                }
            }

            var chestStack = client.player.getItemBySlot(EquipmentSlot.CHEST);
            boolean wearingIcarus = chestStack.is(ModItems.ICARUS_WINGS);
            boolean wearingMechanical = chestStack.is(ModItems.MECHANICAL_WINGS) && hasCoal;

            long ticksSinceFlap = client.player.tickCount - lastFlapClientTick;
            float progress = Math.min((float) ticksSinceFlap / FLAP_COOLDOWN_TICKS, 1.0f);

            if (wearingIcarus || wearingMechanical) {
                int screenWidth = client.getWindow().getGuiScaledWidth();
                int screenHeight = client.getWindow().getGuiScaledHeight();

                int barWidth = 182;
                int x = screenWidth / 2 - 91;
                int y = screenHeight - 32 + 3;

                drawContext.blitSprite(
                        RenderPipelines.GUI_TEXTURED,
                        Identifier.withDefaultNamespace("hud/jump_bar_background"),
                        x, y, barWidth, 5
                );

                int xpBarY = screenHeight - 32;
                int centerX = screenWidth / 2;
                int semiCircleWidth = 15;  // match the texture width
                int semiCircleHeight = 9;  // match the texture height
                int i = centerX - (semiCircleWidth / 2) - 1;
                int j = xpBarY - semiCircleHeight + 3; // adjustment above XP bar

                drawContext.blit(
                    RenderPipelines.GUI_TEXTURED,
                    SEMI_CIRCLE_TEXTURE,
                    i, j,               // screen position
                    0f, 0f,             // u, v in texture
                    semiCircleWidth, semiCircleHeight, // width & height to draw
                    semiCircleWidth, semiCircleHeight  // texture width & height
                );

                if (progress > 0) {
                    int filledWidth = (int) (progress * barWidth);

                    drawContext.blitSprite(
                            RenderPipelines.GUI_TEXTURED,
                            Identifier.withDefaultNamespace("hud/jump_bar_progress"),
                            182, 5,
                            0, 0,
                            x, y,
                            filledWidth, 5
                    );
                }
            }
    }
}
