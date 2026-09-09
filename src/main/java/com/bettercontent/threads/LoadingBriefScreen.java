package com.bettercontent.threads;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

final class LoadingBriefScreen extends Screen {
    private final LoadingBriefSession session;
    private final Runnable dismiss;

    LoadingBriefScreen(LoadingBriefSession session, Runnable dismiss) {
        super(Component.literal(session.current().headline()));
        this.session = session;
        this.dismiss = dismiss;
    }

    @Override
    protected void init() {
        var layout = LoadingBriefLayout.calculate(width, height, false);
        addRenderableWidget(Button.builder(Component.translatable("screen.better_content_threads.loading_previous"), button -> session.move(-1))
            .bounds(width / 2 - 146, layout.controlsY(), 88, 20).build());
        addRenderableWidget(Button.builder(Component.translatable("screen.better_content_threads.loading_continue"), button -> closeBrief())
            .bounds(width / 2 - 50, layout.controlsY(), 100, 20).build());
        addRenderableWidget(Button.builder(Component.translatable("screen.better_content_threads.loading_next"), button -> session.move(1))
            .bounds(width / 2 + 58, layout.controlsY(), 88, 20).build());
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        ThreadClient.renderArrivalBrief(graphics, session, width, height);
        super.render(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_LEFT) {
            session.move(-1);
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_RIGHT) {
            session.move(1);
            return true;
        }
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
