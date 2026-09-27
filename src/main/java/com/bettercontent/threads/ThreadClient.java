package com.bettercontent.threads;

import com.bettercontent.threads.BetterContentThreads;
import com.bettercontent.threads.mixin.LevelLoadingScreenAccessor;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.ConnectScreen;
import net.minecraft.client.gui.screens.LevelLoadingScreen;
import net.minecraft.client.gui.screens.ReceivingLevelScreen;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.FormattedCharSequence;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.event.RegisterClientReloadListenersEvent;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.RenderGuiEvent;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import org.lwjgl.glfw.GLFW;

import java.util.List;

@Mod.EventBusSubscriber(modid = BetterContentThreads.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class ThreadClient {
    static final int ARCHIVE_GOLD = 0xC6A15B;
    static final int ART_TEXTURE_WIDTH = 256;
    static final int ART_TEXTURE_HEIGHT = 384;
    public static final KeyMapping OPEN = new KeyMapping("key.better_content_threads.open_reader", InputConstants.Type.KEYSYM,
        GLFW.GLFW_KEY_M, "key.categories.better_content_threads");
    private static List<ThreadNetwork.Card> cards = List.of();
    private static LoadingBriefSession currentBriefs;
    private static LoadingBriefRotation.State briefState;
    private static boolean briefDisplayed;
    private static boolean emiRecipeOpen;
    private static String emiRecipeTarget = "unknown";

    private ThreadClient() {}

    /** Uses the live KeyMapping so remaps are reflected in every reader prompt. */
    static Component readerBinding() { return OPEN.getTranslatedKeyMessage(); }

    public static void receive(ThreadNetwork.Sync sync) {
        cards = sync.cards();
        if (sync.open()) Minecraft.getInstance().setScreen(new ThreadDeckScreen(cards));
        else if (Minecraft.getInstance().screen instanceof ThreadDeckScreen deck) deck.updateCards(cards);
    }

    @SubscribeEvent
    public static void tick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        var minecraft = Minecraft.getInstance();
        if (briefDisplayed && currentBriefs != null && minecraft.player != null && minecraft.level != null && minecraft.screen == null) {
            // The lesson belongs only to loading.  Once the world can accept input, do
            // not place an opaque screen over it; commit only the exposure already earned.
            dismissBrief();
            return;
        }
        if (OPEN.consumeClick()) ThreadNetwork.request("open", "");
        while (OPEN.consumeClick()) {}
    }

    @SubscribeEvent
    public static void opening(ScreenEvent.Opening event) {
        boolean newRecipe = event.getNewScreen() != null
            && event.getNewScreen().getClass().getName().equals("dev.emi.emi.screen.RecipeScreen");
        if (emiRecipeOpen && !newRecipe) {
            completeEmiRecipe();
        }
        if (newRecipe) {
            emiRecipeOpen = true;
            emiRecipeTarget = EmiRecipeClient.target();
        }
        if (event.getNewScreen() instanceof LevelLoadingScreen levelLoading
                && !(levelLoading instanceof LearningLevelLoadingScreen)
                && Minecraft.getInstance().level == null) {
            if (currentBriefs == null) beginBrief();
            var progress = ((LevelLoadingScreenAccessor) levelLoading).betterContentThreads$progressListener();
            event.setNewScreen(new LearningLevelLoadingScreen(progress, currentBriefs));
            return;
        }
        if (isLoadingScreen(event.getNewScreen())) {
            if (event.getNewScreen() instanceof ConnectScreen || currentBriefs == null) beginBrief();
        } else if (event.getNewScreen() != null && Minecraft.getInstance().player == null) {
            discardBrief();
        }
    }

    @SubscribeEvent
    public static void closing(ScreenEvent.Closing event) {
        if (emiRecipeOpen && event.getScreen().getClass().getName().equals("dev.emi.emi.screen.RecipeScreen")) {
            completeEmiRecipe();
        }
    }

    private static void completeEmiRecipe() {
        String latest = EmiRecipeClient.target();
        if (!latest.equals("unknown")) emiRecipeTarget = latest;
        ThreadNetwork.request("emi", emiRecipeTarget);
        emiRecipeOpen = false;
    }

    @SubscribeEvent
    public static void logout(ClientPlayerNetworkEvent.LoggingOut event) {
        discardBrief();
        cards = List.of();
    }

    @SubscribeEvent
    public static void screen(ScreenEvent.Init.Post event) {
        if (event.getScreen() instanceof PauseScreen) {
            int x = event.getScreen().width - 80;
            int y = 8;
            event.addListener(Button.builder(Component.literal("Threads"), button -> ThreadNetwork.request("open", ""))
                .bounds(x, y, 72, 20).build());
            return;
        }
        if (currentBriefs == null || !isLoadingScreen(event.getScreen())
                || event.getScreen() instanceof LearningLevelLoadingScreen) return;
        var layout = LoadingBriefBackdropLayout.calculate(event.getScreen().width, event.getScreen().height,
            false, nativeControlRows(event.getScreen()));
        event.addListener(Button.builder(Component.translatable("screen.better_content_threads.loading_previous"),
                button -> currentBriefs.move(-1))
            .bounds(event.getScreen().width / 2 - 92, layout.controlsY(), 88, 20).build());
        event.addListener(Button.builder(Component.translatable("screen.better_content_threads.loading_next"),
                button -> currentBriefs.move(1))
            .bounds(event.getScreen().width / 2 + 4, layout.controlsY(), 88, 20).build());
    }

    @SubscribeEvent
    public static void render(RenderGuiEvent.Post event) {
        var minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.screen != null) return;
        renderUnread(event.getGuiGraphics(), event.getWindow().getGuiScaledWidth(), event.getWindow().getGuiScaledHeight());
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

    private static int mix(int value) {
        value ^= value >>> 16;
        value *= 0x7FEB352D;
        value ^= value >>> 15;
        value *= 0x846CA68B;
        return value ^ (value >>> 16);
    }

    private static void renderUnread(GuiGraphics graphics, int screenWidth, int screenHeight) {
        long count = cards.stream().filter(card->card.known()&&card.unread()).count();
        if (count == 0L) return;
        var card = cards.stream().filter(c->c.known()&&c.unread()).findFirst().orElseThrow();
        String binding = readerBinding().getString();
        int keyWidth = Math.min(54, Math.max(14, Minecraft.getInstance().font.width(binding) + 8));
        int labelWidth = Minecraft.getInstance().font.width("Threads");
        var layout = UnreadBadgeLayout.calculate(screenWidth, keyWidth, labelWidth);
        int x = layout.plateX();
        int y = Math.max(36, screenHeight / 2 - 14);
        renderSealedPlate(graphics, x, y, 18, 27, ThreadTopic.parse(card.topic()).color(),card.aspect().isEmpty()?ARCHIVE_GOLD:ThreadAspect.parse(card.aspect()).color(), card.id().hashCode(), false);
        graphics.drawString(Minecraft.getInstance().font, Long.toString(count), x + 13, y + 19, 0xFFF0E5CE, true);
        drawKeycap(graphics, binding, layout.contentX(), y + 3, layout.contentWidth());
        if (layout.showLabel()) graphics.drawString(Minecraft.getInstance().font, "Threads", layout.contentX(), y + 18, 0xFFE0D4BB, true);
    }

    static void drawKeycap(GuiGraphics graphics,String binding,int x,int y,int width){
        graphics.fill(x,y,x+width,y+13,0xE0C6A15B);
        graphics.fill(x+1,y+1,x+width-1,y+12,0xF0121513);
        String label=binding;
        while(label.length()>1&&Minecraft.getInstance().font.width(label)>width-4)label=label.substring(0,label.length()-1);
        graphics.drawCenteredString(Minecraft.getInstance().font,label,x+width/2,y+3,0xFFF0E5CE);
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
            Component.translatable("screen.better_content_threads.generating_world"), screenWidth / 2,
            layout.headerY(), 1.0f, 1.0f);
        int percent = Mth.clamp(progress, 0, 100);
        graphics.fill(layout.barX() - 1, layout.barY() - 1, layout.barX() + layout.barWidth() + 1,
            layout.barY() + 9, 0xFFC6A15B);
        graphics.fill(layout.barX(), layout.barY(), layout.barX() + layout.barWidth(), layout.barY() + 8, 0xFF222923);
        graphics.fill(layout.barX(), layout.barY(), layout.barX() + Math.round(layout.barWidth() * percent / 100.0f),
            layout.barY() + 8, 0xFF8E5BB7);
        drawOutlinedCentered(graphics,
            Component.translatable("screen.better_content_threads.building_spawn", percent), screenWidth / 2,
            layout.barY() + 11, 1.0f, 1.0f);
        renderLoadingCaption(graphics, session, layout);
    }

    static void renderArrivalBrief(GuiGraphics graphics, LoadingBriefSession session, int screenWidth, int screenHeight) {
        var layout = LoadingBriefBackdropLayout.calculate(screenWidth, screenHeight, false);
        renderLoadingBackdrop(graphics, session, layout, screenWidth, screenHeight);
        drawOutlinedCentered(graphics,
            Component.translatable("screen.better_content_threads.world_ready"), screenWidth / 2,
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
        graphics.drawString(font, Component.translatable("screen.better_content_threads.lesson_count",
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

    static void renderSealedPlate(GuiGraphics graphics,int x,int y,int width,int height,int suitColor,int aspectColor,int seed,boolean selected) {
        graphics.fill(x-2,y-2,x+width+2,y+height+2,((selected?0xCC:0x78)<<24)|suitColor);
        graphics.fill(x, y, x + width, y + height, 0xFF111513);
        int traceAlpha = selected ? 0xB0 : 0x78;
        for (int i = 0; i < 5; i++) {
            int mixed = mix(seed + i * 71);
            int tx = x + 2 + Math.floorMod(mixed, Math.max(1, width - 4));
            int ty = y + 2 + i * Math.max(1, (height - 5) / 5);
            graphics.fill(tx, ty, Math.min(x + width - 1, tx + 2), ty + 1, (traceAlpha << 24) | aspectColor);
        }
    }

    static void renderArt(GuiGraphics graphics, String art, int x, int y, int width, int height) {
        var id = ResourceLocation.tryParse(art);
        if (id != null) graphics.blit(id, x, y, width, height, 0.0f, 0.0f,
            ART_TEXTURE_WIDTH, ART_TEXTURE_HEIGHT, ART_TEXTURE_WIDTH, ART_TEXTURE_HEIGHT);
    }

    static void renderArt(GuiGraphics graphics, String art, int x, int y, int width, int height, float alpha) {
        graphics.flush();
        com.mojang.blaze3d.systems.RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, alpha);
        renderArt(graphics, art, x, y, width, height);
        graphics.flush();
        com.mojang.blaze3d.systems.RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
    }

    static String layer(String art, String layer) {
        return art.endsWith(".png") ? art.substring(0, art.length() - 4) + "_" + layer + ".png" : art + "_" + layer;
    }

    @Mod.EventBusSubscriber(modid = BetterContentThreads.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static final class ModEvents {
        @SubscribeEvent
        public static void keys(RegisterKeyMappingsEvent event) {
            event.register(OPEN);
        }

        @SubscribeEvent
        public static void reload(RegisterClientReloadListenersEvent event) {
            event.registerReloadListener(LoadingBriefs.INSTANCE);
            event.registerReloadListener(DeathHints.INSTANCE);
        }

        @SubscribeEvent
        public static void setup(FMLClientSetupEvent event) {
            event.enqueueWork(() -> ItemProperties.register(ThreadRegistry.FACSIMILE.get(),
                new ResourceLocation(BetterContentThreads.MOD_ID, "thread_index"),
                (stack, level, entity, seed) -> ThreadArt.itemIndex(ThreadFacsimileItem.threadId(stack))));
        }
    }
}
