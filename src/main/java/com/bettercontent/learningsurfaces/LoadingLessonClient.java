package com.bettercontent.learningsurfaces;

import com.bettercontent.learningsurfaces.mixin.LevelLoadingScreenAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.ConnectScreen;
import net.minecraft.client.gui.screens.LevelLoadingScreen;
import net.minecraft.client.gui.screens.ReceivingLevelScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** Loading lessons, their exposure clock, and Lessons presentation. */
@Mod.EventBusSubscriber(modid = LearningSurfaces.MOD_ID, value = Dist.CLIENT)
public final class LoadingLessonClient {
    private static LoadingBriefSession currentBriefs;
    private static LoadingBriefRotation.State briefState;
    private static boolean briefDisplayed;

    private LoadingLessonClient() {}

    @SubscribeEvent public static void tick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        var minecraft = Minecraft.getInstance();
        if (briefDisplayed && currentBriefs != null && minecraft.player != null
                && minecraft.level != null && minecraft.screen == null) dismissBrief();
    }

    @SubscribeEvent public static void opening(ScreenEvent.Opening event) {
        if (event.getNewScreen() instanceof LevelLoadingScreen levelLoading
                && !(levelLoading instanceof LearningLevelLoadingScreen)
                && Minecraft.getInstance().level == null) {
            if (currentBriefs == null) beginBrief();
            var progress = ((LevelLoadingScreenAccessor) levelLoading).learningSurfaces$progressListener();
            event.setNewScreen(new LearningLevelLoadingScreen(progress, currentBriefs));
            return;
        }
        if (isLoadingScreen(event.getNewScreen())) {
            if (event.getNewScreen() instanceof ConnectScreen || currentBriefs == null) beginBrief();
        } else if (event.getNewScreen() != null && Minecraft.getInstance().player == null) {
            discardBrief();
        }
    }

    @SubscribeEvent public static void logout(ClientPlayerNetworkEvent.LoggingOut event) {
        discardBrief();
    }

    @SubscribeEvent public static void screen(ScreenEvent.Init.Post event) {
        if (currentBriefs == null || !isLoadingScreen(event.getScreen())
                || event.getScreen() instanceof LearningLevelLoadingScreen) return;
        var layout = LoadingBriefBackdropLayout.calculate(event.getScreen().width, event.getScreen().height,
            false, nativeControlRows(event.getScreen()));
        event.addListener(Button.builder(Component.translatable("screen.learning_surfaces.loading_previous"),
                button -> currentBriefs.move(-1))
            .bounds(event.getScreen().width / 2 - 92, layout.controlsY(), 88, 20).build());
        event.addListener(Button.builder(Component.translatable("screen.learning_surfaces.loading_next"),
                button -> currentBriefs.move(1))
            .bounds(event.getScreen().width / 2 + 4, layout.controlsY(), 88, 20).build());
    }

    private static void drawOutlinedCentered(GuiGraphics graphics, Component text, int centerX, int y,
            float scale, float alpha) {
        int textWidth = Minecraft.getInstance().font.width(text);
        int colorAlpha = Math.round(alpha * 255.0f) << 24;
        graphics.pose().pushPose();
        graphics.pose().translate(centerX, y, 0);
        graphics.pose().scale(scale, scale, 1.0f);
        for (int ox = -1; ox <= 1; ox++) for (int oy = -1; oy <= 1; oy++) {
            if (ox != 0 || oy != 0) graphics.drawString(Minecraft.getInstance().font, text,
                -textWidth / 2 + ox, oy, colorAlpha, false);
        }
        graphics.drawString(Minecraft.getInstance().font, text, -textWidth / 2, 0,
            colorAlpha | 0xFFFFFF, false);
        graphics.pose().popPose();
    }

    private static boolean isLoadingScreen(net.minecraft.client.gui.screens.Screen screen) {
        return screen instanceof ConnectScreen || screen instanceof ReceivingLevelScreen || screen instanceof LevelLoadingScreen;
    }

    private static int nativeControlRows(net.minecraft.client.gui.screens.Screen screen) {
        return screen instanceof ConnectScreen ? 2 : 1;
    }

    private static void beginBrief() {
        briefState = LoadingBriefStore.load();
        currentBriefs = new LoadingBriefSession(LoadingBriefs.INSTANCE.all(), briefState);
        briefDisplayed = false;
    }

    private static void discardBrief() {
        currentBriefs = null;
        briefDisplayed = false;
    }

    private static void dismissBrief() {
        if (currentBriefs == null) return;
        briefState = LoadingBriefRotation.commit(briefState == null ? LoadingBriefStore.load() : briefState,
            currentBriefs.viewed(), LoadingBriefs.INSTANCE.all());
        LoadingBriefStore.save(briefState);
        currentBriefs = null;
        briefDisplayed = false;
    }

    static void renderWorldGenerationBrief(GuiGraphics graphics, LoadingBriefSession session, int progress,
                                             int screenWidth, int screenHeight) {
        if (currentBriefs == session) briefDisplayed = true;
        var layout = LoadingBriefBackdropLayout.calculate(screenWidth, screenHeight, true);
        renderLoadingBackdrop(graphics, session, layout, screenWidth, screenHeight);
        drawOutlinedCentered(graphics,
            Component.translatable("screen.learning_surfaces.generating_world"), screenWidth / 2,
            layout.headerY(), 1.0f, 1.0f);
        int percent = Mth.clamp(progress, 0, 100);
        graphics.fill(layout.barX() - 1, layout.barY() - 1, layout.barX() + layout.barWidth() + 1,
            layout.barY() + 9, 0xFFC6A15B);
        graphics.fill(layout.barX(), layout.barY(), layout.barX() + layout.barWidth(), layout.barY() + 8, 0xFF222923);
        graphics.fill(layout.barX(), layout.barY(), layout.barX() + Math.round(layout.barWidth() * percent / 100.0f),
            layout.barY() + 8, 0xFF8E5BB7);
        drawOutlinedCentered(graphics,
            Component.translatable("screen.learning_surfaces.building_spawn", percent), screenWidth / 2,
            layout.barY() + 11, 1.0f, 1.0f);
        renderLoadingCaption(graphics, session, layout);
    }

    static void renderArrivalBrief(GuiGraphics graphics, LoadingBriefSession session, int screenWidth, int screenHeight) {
        var layout = LoadingBriefBackdropLayout.calculate(screenWidth, screenHeight, false);
        renderLoadingBackdrop(graphics, session, layout, screenWidth, screenHeight);
        drawOutlinedCentered(graphics,
            Component.translatable("screen.learning_surfaces.world_ready"), screenWidth / 2,
            layout.headerY(), 1.0f, 1.0f);
        renderLoadingCaption(graphics, session, layout);
    }

    public static boolean renderJoiningBrief(GuiGraphics graphics, Component status, int screenWidth, int screenHeight) {
        return renderNativeLoadingBrief(graphics, status, screenWidth, screenHeight, 2);
    }

    public static boolean renderTerrainBrief(GuiGraphics graphics, int screenWidth, int screenHeight) {
        return renderNativeLoadingBrief(graphics, Component.translatable("multiplayer.downloadingTerrain"),
            screenWidth, screenHeight, 1);
    }

    private static boolean renderNativeLoadingBrief(GuiGraphics graphics, Component status,
                                                     int screenWidth, int screenHeight, int controlRows) {
        var session = currentBriefs;
        if (session == null) return false;
        briefDisplayed = true;
        var layout = LoadingBriefBackdropLayout.calculate(screenWidth, screenHeight, false,
            controlRows);
        renderLoadingBackdrop(graphics, session, layout, screenWidth, screenHeight);
        drawOutlinedCentered(graphics, status, screenWidth / 2, layout.headerY(), 1.0f, 1.0f);
        renderLoadingCaption(graphics, session, layout);
        return true;
    }

    private static void renderLoadingBackdrop(GuiGraphics graphics, LoadingBriefSession session,
                                               LoadingBriefBackdropLayout layout, int screenWidth,
                                               int screenHeight) {
        graphics.fill(0, 0, screenWidth, screenHeight, 0xFF0B0E0C);
        graphics.blit(session.current().art(), layout.artX(), layout.artY(), layout.artWidth(), layout.artHeight(),
            0, 0, 512, 256, 512, 256);
    }

    private static void renderLoadingCaption(GuiGraphics graphics, LoadingBriefSession session,
                                              LoadingBriefBackdropLayout layout) {
        session.observe(net.minecraft.Util.getMillis());
        var font = Minecraft.getInstance().font;
        var brief = session.current();
        var title = font.split(Component.literal(brief.headline()), layout.textWidth());
        var body = font.split(Component.literal(lessonBody(brief)), layout.textWidth());
        var action = font.split(Component.literal("TRY THIS: " + lessonAction(brief)), layout.textWidth());
        int needed = 34 + (title.size() + body.size() + action.size()) * 10;
        int bottom = layout.captionY() + layout.captionHeight();
        int top = Math.max(layout.barY() > 0 ? layout.barY() + 28 : 26, bottom - needed);
        graphics.fill(layout.captionX(), top, layout.captionX() + layout.captionWidth(), bottom, 0xEC101412);
        int lineY = top + 8;
        graphics.drawString(font, Component.translatable("screen.learning_surfaces.lesson_count",
            brief.category().toUpperCase(), session.index() + 1, session.size()), layout.textX(), lineY, 0xFFBAB8AB, false);
        lineY += 14;
        for (var line : title) { graphics.drawString(font, line, layout.textX(), lineY, 0xFFF0E5CE, false); lineY += 10; }
        lineY += 4;
        for (var line : body) { graphics.drawString(font, line, layout.textX(), lineY, 0xFFF0E5CE, false); lineY += 10; }
        lineY += 4;
        for (var line : action) { graphics.drawString(font, line, layout.textX(), lineY, 0xFFE7CA8B, false); lineY += 10; }
    }

    static String lessonBody(LoadingBrief brief) {
        if (brief.id().equals("movement")) return brief.body().replace("uses Shift", "uses " + keyLabel("key.parcool.FastRun", "Shift"))
            .replace("uses Mouse 5", "uses " + keyLabel("key.parcool.Vault", "Mouse 5"))
            .replace("uses R;", "uses " + keyLabel("key.parcool.Dodge", "R") + ";");
        if (brief.id().equals("plonk")) return brief.body().replace("the shown key", keyLabel("key.plonk.place", "Minus"));
        return brief.body();
    }

    static String lessonAction(LoadingBrief brief) {
        return brief.id().equals("plonk")
            ? brief.action().replace("the shown key", keyLabel("key.plonk.place", "Minus"))
            : brief.action();
    }

    private static String keyLabel(String name, String fallback) {
        for (var key : Minecraft.getInstance().options.keyMappings)
            if (key.getName().equals(name)) return key.isUnbound() ? "an unbound key (set it in Controls)" : key.getTranslatedKeyMessage().getString();
        return fallback;
    }

    static ReadingText lessonText(LoadingBriefSession session, int width) {
        var brief = session.current();
        var text = new ReadingText(Minecraft.getInstance().font, width);
        text.add(brief.category().toUpperCase() + " · LESSON " + (session.index() + 1) + " OF " + session.size(), 0xFFBAB8AB);
        text.gap();
        text.add(brief.headline(), 0xFFF0E5CE);
        text.gap();
        text.add(lessonBody(brief), 0xFFF0E5CE);
        text.gap();
        text.add("TRY THIS: " + lessonAction(brief), 0xFFE7CA8B);
        return text;
    }

    static void renderLessonCard(GuiGraphics graphics, LoadingBriefSession session, LoadingBriefLayout layout, int scroll) {
        graphics.fill(layout.panelX() - 1, layout.panelY() - 1,
            layout.panelX() + layout.panelWidth() + 1, layout.panelY() + layout.panelHeight() + 1, 0xFF746344);
        graphics.fill(layout.panelX(), layout.panelY(), layout.panelX() + layout.panelWidth(),
            layout.panelY() + layout.panelHeight(), 0xFF101412);
        if (layout.showArt()) graphics.blit(session.current().art(), layout.artX(), layout.artY(), layout.artWidth(), layout.artHeight(),
            0, 0, 512, 256, 512, 256);
        lessonText(session, layout.textWidth() - 8).render(graphics, layout.textX(), layout.textY(),
            layout.textWidth(), layout.panelHeight() - 24, scroll);
    }

}
