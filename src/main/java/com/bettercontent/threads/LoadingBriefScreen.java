package com.bettercontent.threads;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

final class LoadingBriefScreen extends Screen {
    private final LoadingBrief brief;
    private final Runnable dismiss;

    LoadingBriefScreen(LoadingBrief brief, Runnable dismiss) {
        super(Component.literal(brief.headline()));
        this.brief = brief;
        this.dismiss = dismiss;
    }

    @Override
    protected void init() {
        addRenderableWidget(Button.builder(Component.literal("Continue"), button -> closeBrief())
            .bounds(width / 2 - 50, Math.min(height - 32, height / 2 + 96), 100, 20).build());
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        ThreadClient.renderLoadingBrief(graphics, brief, width, height, true);
        super.render(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER || keyCode == GLFW.GLFW_KEY_SPACE) {
            closeBrief();
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) return true;
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override public boolean shouldCloseOnEsc() { return false; }
    @Override public boolean isPauseScreen() { return true; }

    private void closeBrief() {
        dismiss.run();
        Minecraft.getInstance().setScreen(null);
    }
}
