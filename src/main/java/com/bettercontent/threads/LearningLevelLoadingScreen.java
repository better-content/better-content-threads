package com.bettercontent.threads;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.server.level.progress.StoringChunkProgressListener;
import org.lwjgl.glfw.GLFW;

final class LearningLevelLoadingScreen extends net.minecraft.client.gui.screens.LevelLoadingScreen {
    private final StoringChunkProgressListener progressListener;
    private final LoadingBriefSession session;
    private Button keepReading;
    private long lastProgressNarration;

    LearningLevelLoadingScreen(StoringChunkProgressListener progressListener, LoadingBriefSession session) {
        super(progressListener);
        this.progressListener = progressListener;
        this.session = session;
    }

    @Override
    protected void init() {
        var layout = LoadingBriefBackdropLayout.calculate(width, height, true);
        addRenderableWidget(Button.builder(net.minecraft.network.chat.Component.translatable("screen.better_content_threads.loading_previous"),
                button -> session.move(-1))
            .bounds(width / 2 - 146, layout.controlsY(), 88, 20).build());
        keepReading = addRenderableWidget(Button.builder(ThreadClient.keepReadingLabel(session), button -> {
                session.toggleKeepReading();
                button.setMessage(ThreadClient.keepReadingLabel(session));
            })
            .bounds(width / 2 - 50, layout.controlsY(), 100, 20).build());
        addRenderableWidget(Button.builder(net.minecraft.network.chat.Component.translatable("screen.better_content_threads.loading_next"),
                button -> session.move(1))
            .bounds(width / 2 + 58, layout.controlsY(), 88, 20).build());
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        long now = net.minecraft.Util.getMillis();
        if (now - lastProgressNarration > 2_000L) {
            lastProgressNarration = now;
            triggerImmediateNarration(true);
        }
        // This screen owns the complete loading presentation; the vanilla percentage
        // otherwise survives above the illustration as a second progress label.
        ThreadClient.renderWorldGenerationBrief(graphics, session, progressListener.getProgress(), width, height);
        for (var renderable : renderables) renderable.render(graphics, mouseX, mouseY, partialTick);
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
        return super.keyPressed(keyCode, scanCode, modifiers);
    }
}
