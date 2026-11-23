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

public class IcarusWingsClient implements ClientModInitializer {
    private static final Identifier FLAP_BAR_ID = IcarusWings.id("flap_cooldown_bar");
    private static final Identifier SEMI_CIRCLE_TEXTURE = IcarusWings.id("textures/gui/semi_circle.png");
    private static final Identifier BAR_BACKGROUND_SPRITE = Identifier.withDefaultNamespace("hud/jump_bar_background");
    private static final Identifier BAR_PROGRESS_SPRITE = Identifier.withDefaultNamespace("hud/jump_bar_progress");

    private static final int BAR_WIDTH = 182;
    private static final int BAR_HEIGHT = 5;
    private static final int SEMI_CIRCLE_WIDTH = 15;  // matches the texture width
    private static final int SEMI_CIRCLE_HEIGHT = 9;  // matches the texture height

    private boolean wasJumpPressed = false;
    private long lastFlapClientTick = -100;
    private boolean hudErrorLogged = false;

    @Override
    public void onInitializeClient() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null) return;

            boolean jumpPressed = client.options.keyJump.isDown();

            if (jumpPressed && !wasJumpPressed && client.player.isFallFlying()) {
                long currentTick = client.player.tickCount;
                if (currentTick - lastFlapClientTick >= WingUtil.FLAP_COOLDOWN_TICKS) {
                    ClientPlayNetworking.send(new FlapPayload());
                    lastFlapClientTick = currentTick;
                }
            }
            wasJumpPressed = jumpPressed;
        });

        // HUD is retained-mode via HudElementRegistry; addLast draws after the vanilla HUD.
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

    private void extractFlapBar(GuiGraphicsExtractor graphics) {
        var client = Minecraft.getInstance();
        if (client.player == null) return;

        var chestStack = client.player.getItemBySlot(EquipmentSlot.CHEST);
        boolean wearingIcarus = chestStack.is(ModItems.ICARUS_WINGS);
        boolean wearingMechanical = chestStack.is(ModItems.MECHANICAL_WINGS)
                && WingUtil.hasFuel(client.player.getInventory());
        if (!wearingIcarus && !wearingMechanical) return;

        long ticksSinceFlap = client.player.tickCount - lastFlapClientTick;
        float progress = Math.min((float) ticksSinceFlap / WingUtil.FLAP_COOLDOWN_TICKS, 1.0f);

        int screenWidth = client.getWindow().getGuiScaledWidth();
        int screenHeight = client.getWindow().getGuiScaledHeight();
        int centerX = screenWidth / 2;
        int xpBarY = screenHeight - 32;

        int x = centerX - 91;
        int y = xpBarY + 3;
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, BAR_BACKGROUND_SPRITE, x, y, BAR_WIDTH, BAR_HEIGHT);

        // semi-circle marker sitting just above the XP bar
        int markerX = centerX - (SEMI_CIRCLE_WIDTH / 2) - 1;
        int markerY = xpBarY - SEMI_CIRCLE_HEIGHT + 3;
        graphics.blit(RenderPipelines.GUI_TEXTURED, SEMI_CIRCLE_TEXTURE,
                markerX, markerY, 0f, 0f,
                SEMI_CIRCLE_WIDTH, SEMI_CIRCLE_HEIGHT, SEMI_CIRCLE_WIDTH, SEMI_CIRCLE_HEIGHT);

        if (progress > 0) {
            int filledWidth = (int) (progress * BAR_WIDTH);
            graphics.blitSprite(RenderPipelines.GUI_TEXTURED, BAR_PROGRESS_SPRITE,
                    BAR_WIDTH, BAR_HEIGHT, 0, 0, x, y, filledWidth, BAR_HEIGHT);
        }
    }
}
