package com.bettercontent.threads;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/** Production hint renderer with vanilla death-control geometry; needs no player or world. */
final class DeathHintVisualScreen extends Screen {
    private final DeathHint hint;
    private final boolean hardcore;

    DeathHintVisualScreen(DeathHint hint, boolean hardcore) {
        super(Component.translatable(hardcore ? "deathScreen.title.hardcore" : "deathScreen.title"));
        this.hint = hint;
        this.hardcore = hardcore;
    }
    @Override protected void init() {
        addRenderableWidget(Button.builder(Component.translatable(hardcore ? "deathScreen.spectate" : "deathScreen.respawn"), b -> {})
            .bounds(width / 2 - 100, height / 4 + 72, 200, 20).build());
        addRenderableWidget(Button.builder(Component.translatable("deathScreen.titleScreen"), b -> {})
            .bounds(width / 2 - 100, height / 4 + 96, 200, 20).build());
    }
    @Override public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        graphics.fillGradient(0, 0, width, height, 0xFF4B1717, 0xFF241414);
        graphics.pose().pushPose();
        graphics.pose().scale(2, 2, 2);
        graphics.drawCenteredString(font, title, width / 4, 30, 0xFFFFFF);
        graphics.pose().popPose();
        graphics.drawCenteredString(font, Component.literal("Death-screen hint layout review"), width / 2, 85, 0xFFFFFF);
        super.render(graphics, mouseX, mouseY, partialTick);
        boolean visible = DeathHintClient.renderHint(graphics, this, hint);
        if (height >= 240 && !visible) throw new IllegalStateException("Hint did not fit: " + hint.id() + " " + width + "x" + height);
    }
}
