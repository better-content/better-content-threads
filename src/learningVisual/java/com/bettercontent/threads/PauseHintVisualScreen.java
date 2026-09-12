package com.bettercontent.threads;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/** Single/multiplayer control-geometry fixtures complement the real PauseScreen capture. */
final class PauseHintVisualScreen extends Screen {
    private final DeathHint hint;
    private final boolean singleplayer;
    PauseHintVisualScreen(DeathHint hint, boolean singleplayer) {
        super(Component.translatable("menu.game"));
        this.hint = hint;
        this.singleplayer = singleplayer;
    }
    @Override protected void init() {
        addRenderableWidget(new StringWidget(0, 40, width, 9, title, font));
        int y = Math.round((height - 190) * 0.25f) + 50;
        full("Back to Game", y);
        pair("Advancements", "Statistics", y + 24);
        pair("Give Feedback", "Report Bugs", y + 48);
        pair("Options...", singleplayer ? "Open to LAN" : "Player Reporting", y + 72);
        full("Mods", y + 96);
        full(singleplayer ? "Save and Quit to Title" : "Disconnect", y + 120);
        addRenderableWidget(Button.builder(Component.literal("Threads"), b -> {}).bounds(width - 80, 8, 72, 20).build());
        DeathHintClient.rememberPauseLayout(this);
        // Check all authored copy with the real font, including current binding expansion.
        for (var candidate : DeathHints.INSTANCE.all()) {
            int lines = font.split(DeathHintClient.text(candidate), DeathHintLayout.textWidth(width)).size();
            if (!PauseHintLayout.calculate(height, y, y + 140, lines, font.lineHeight).fits())
                throw new IllegalStateException("Pause tip does not fit: " + candidate.id() + " " + width + "x" + height);
        }
    }
    private void full(String text, int y) {
        addRenderableWidget(Button.builder(Component.literal(text), b -> {}).bounds(width / 2 - 102, y, 204, 20).build());
    }
    private void pair(String left, String right, int y) {
        addRenderableWidget(Button.builder(Component.literal(left), b -> {}).bounds(width / 2 - 102, y, 98, 20).build());
        addRenderableWidget(Button.builder(Component.literal(right), b -> {}).bounds(width / 2 + 4, y, 98, 20).build());
    }
    @Override public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        graphics.fill(0, 0, width, height, 0xFF191C21);
        DeathHintClient.layoutPause(this, hint);
        super.render(graphics, mouseX, mouseY, partialTick);
        if (!DeathHintClient.renderHint(graphics, this, hint)) throw new IllegalStateException("Pause tip unexpectedly hidden");
    }
}
