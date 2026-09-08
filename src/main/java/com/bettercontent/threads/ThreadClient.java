package com.bettercontent.threads;

import com.bettercontent.threads.BetterContentThreads;
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
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
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
    static final int NOTICE_GLYPH_SIZE = 8;
    static final float NOTICE_TEXT_SCALE = 0.72f;
    static final float NOTICE_MIN_TEXT_SCALE = 0.55f;
    static final int NOTICE_GLYPH_OFFSET_Y = -8;
    static final int NOTICE_TEXT_OFFSET_Y = 2;
    static final int NOTICE_HINT_OFFSET_Y = 10;
    static final float NOTICE_HINT_SCALE = 0.58f;
    static final int ARCHIVE_GOLD = 0xC6A15B;
    static final int ART_TEXTURE_WIDTH = 256;
    static final int ART_TEXTURE_HEIGHT = 384;
    public static final KeyMapping OPEN = new KeyMapping("key.better_content_threads.open_reader", InputConstants.Type.KEYSYM,
        GLFW.GLFW_KEY_M, "key.categories.better_content_threads");
    private static final ThreadNoticeQueue<ThreadNetwork.Notice> NOTICES = new ThreadNoticeQueue<>(ThreadNetwork.Notice::identity);
    private static List<ThreadNetwork.Card> cards = List.of();
    private static long lastLiveFrame;
    private static LoadingBrief currentBrief;
    private static LoadingBriefRotation.State briefState;
    private static boolean arrivalPending;

    private ThreadClient() {}

    public static void receive(ThreadNetwork.Sync sync) {
        cards = sync.cards();
        NOTICES.addAll(sync.notices());
        if (sync.open()) Minecraft.getInstance().setScreen(new ThreadDeckScreen(cards));
    }

    @SubscribeEvent
    public static void tick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        var minecraft = Minecraft.getInstance();
        if (arrivalPending && currentBrief != null && minecraft.player != null && minecraft.level != null && minecraft.screen == null) {
            arrivalPending = false;
            minecraft.setScreen(new LoadingBriefScreen(currentBrief, ThreadClient::dismissBrief));
            return;
        }
        if (OPEN.consumeClick()) ThreadNetwork.request("open", "");
        while (OPEN.consumeClick()) {}
        if (minecraft.screen != null) lastLiveFrame = 0L;
    }

    @SubscribeEvent
    public static void opening(ScreenEvent.Opening event) {
        if (event.getNewScreen() != null && event.getNewScreen().getClass().getName().equals("dev.emi.emi.screen.RecipeScreen")) {
            ThreadNetwork.request("recipe_viewed", "");
        }
        if (isInitialLoadScreen(event.getNewScreen()) && Minecraft.getInstance().level == null) {
            if (event.getNewScreen() instanceof ConnectScreen || currentBrief == null) beginBrief();
        }
    }

    @SubscribeEvent
    public static void loadingRender(ScreenEvent.Render.Post event) {
        if (currentBrief != null && isInitialLoadScreen(event.getScreen()) && Minecraft.getInstance().level == null) {
            renderLoadingBrief(event.getGuiGraphics(), currentBrief, event.getScreen().width, event.getScreen().height, false);
        }
    }

    @SubscribeEvent
    public static void login(ClientPlayerNetworkEvent.LoggingIn event) {
        if (currentBrief == null) beginBrief();
        arrivalPending = true;
    }

    @SubscribeEvent
    public static void logout(ClientPlayerNetworkEvent.LoggingOut event) {
        currentBrief = null;
        arrivalPending = false;
    }

    @SubscribeEvent
    public static void screen(ScreenEvent.Init.Post event) {
        if (!(event.getScreen() instanceof PauseScreen)) return;
        int x = event.getScreen().width / 2 + 104;
        int y = event.getScreen().height / 4 + 120;
        event.addListener(Button.builder(Component.literal("Threads"), button -> ThreadNetwork.request("open", ""))
            .bounds(x, y, 72, 20).build());
    }

    @SubscribeEvent
    public static void render(RenderGuiEvent.Post event) {
        var minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.screen != null) {
            lastLiveFrame = 0L;
            return;
        }
        long now = System.currentTimeMillis();
        long delta = lastLiveFrame == 0L ? 0L : Math.min(100L, Math.max(0L, now - lastLiveFrame));
        lastLiveFrame = now;
        var frame = NOTICES.advance(delta, false);
        if (frame != null) {
            if (frame.started()) minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.BOOK_PAGE_TURN, 0.72f, 0.38f));
            renderNotice(event.getGuiGraphics(), frame.notice(), frame.elapsedMs(), event.getWindow().getGuiScaledWidth(), event.getWindow().getGuiScaledHeight());
        }
        renderUnread(event.getGuiGraphics(), event.getWindow().getGuiScaledWidth(), event.getWindow().getGuiScaledHeight());
    }

    private static void renderNotice(GuiGraphics graphics, ThreadNetwork.Notice notice, long elapsed, int screenWidth, int screenHeight) {
        float alpha = noticeAlpha(elapsed);
        int centerX = screenWidth / 2;
        int centerY = screenHeight / 3;
        renderParticles(graphics, notice, elapsed, alpha, centerX, centerY - 5);
        drawArchiveGlyph(graphics,centerX-4,centerY+NOTICE_GLYPH_OFFSET_Y,ARCHIVE_GOLD,alpha);
        Component message = Component.translatable(notice.kind()==ThreadNetwork.NoticeKind.REVEAL?"message.better_content_threads.thread_revealed":"message.better_content_threads.thread_completed", notice.title());
        int textWidth = Minecraft.getInstance().font.width(message);
        float scale = Math.max(NOTICE_MIN_TEXT_SCALE,Math.min(NOTICE_TEXT_SCALE, (screenWidth - 24.0f) / Math.max(1, textWidth)));
        drawOutlinedCentered(graphics, message, centerX, centerY + NOTICE_TEXT_OFFSET_Y, scale, alpha);
        Component hint = Component.translatable("message.better_content_threads.thread_reader_hint",
                OPEN.getTranslatedKeyMessage());
        int hintWidth = Minecraft.getInstance().font.width(hint);
        float hintScale = Math.min(NOTICE_HINT_SCALE, (screenWidth - 24.0f) / Math.max(1, hintWidth));
        drawOutlinedCentered(graphics, hint, centerX, centerY + NOTICE_HINT_OFFSET_Y, hintScale, alpha * 0.82f);
    }

    private static void drawArchiveGlyph(GuiGraphics graphics,int x,int y,int rgb,float alpha){int color=(Math.round(alpha*255)<<24)|rgb;int[][]rows={{3,4},{2,5},{1,3,4,6},{0,2,5,7},{0,2,5,7},{1,3,4,6},{2,5},{3,4}};for(int py=0;py<rows.length;py++)for(int px:rows[py])graphics.fill(x+px,y+py,x+px+1,y+py+1,color);}

    private static void drawOutlinedCentered(GuiGraphics graphics, Component text, int centerX, int y, float scale, float alpha) {
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

    static float noticeAlpha(long elapsed) {
        if (elapsed < 400L) return elapsed / 400.0f;
        if (elapsed < 2_600L) return 1.0f;
        return Math.max(0.0f, (ThreadNoticeQueue.DURATION_MS - elapsed) / 600.0f);
    }

    private static void renderParticles(GuiGraphics graphics, ThreadNetwork.Notice notice, long elapsed, float noticeAlpha, int centerX, int centerY) {
        int aspect = ThreadAspect.parse(notice.aspect()).color();
        double progress = elapsed / (double) ThreadNoticeQueue.DURATION_MS;
        int seed = notice.id().hashCode();
        for (int i = 0; i < 20; i++) {
            int mixed = mix(seed + i * 0x9E3779B9);
            double angle = ((mixed & 0xFFFF) / 65535.0) * Math.PI * 2.0;
            double baseRadius = 6.0 + ((mixed >>> 16) & 3);
            double drift = progress * (4.0 + ((mixed >>> 20) & 3));
            int x = centerX + (int) Math.round(Math.cos(angle) * (baseRadius + drift));
            int y = centerY + (int) Math.round(Math.sin(angle) * baseRadius - progress * (6.0 + ((mixed >>> 24) & 3)));
            float pulse = (float) (0.58 + 0.42 * Math.sin(Math.PI * Math.min(1.0, progress * 1.25 + (i % 4) * 0.06)));
            int particleAlpha = (int) (noticeAlpha * pulse * (i < 12 ? 150 : 190));
            int rgb = i < 12 ? aspect : ARCHIVE_GOLD;
            graphics.fill(x, y, x + 1, y + 1, (particleAlpha << 24) | rgb);
        }
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
        String binding = OPEN.getTranslatedKeyMessage().getString();
        int keyWidth = Math.min(54, Math.max(14, Minecraft.getInstance().font.width(binding) + 8));
        int totalWidth = 18 + 5 + keyWidth + 5;
        int x = screenWidth - totalWidth - 6;
        int y = Math.max(36, screenHeight / 2 - 14);
        renderSealedPlate(graphics, x, y, 18, 27, ThreadSuit.parse(card.suit()).color(),ThreadAspect.parse(card.aspect()).color(), card.id().hashCode(), false);
        graphics.drawString(Minecraft.getInstance().font, Long.toString(count), x + 13, y + 19, 0xFFF0E5CE, true);
        drawKeycap(graphics, binding, x + 23, y + 3, keyWidth);
        graphics.drawString(Minecraft.getInstance().font, "Threads", x + 23, y + 18, 0xFFE0D4BB, true);
    }

    static void drawKeycap(GuiGraphics graphics,String binding,int x,int y,int width){
        graphics.fill(x,y,x+width,y+13,0xE0C6A15B);
        graphics.fill(x+1,y+1,x+width-1,y+12,0xF0121513);
        String label=binding;
        while(label.length()>1&&Minecraft.getInstance().font.width(label)>width-4)label=label.substring(0,label.length()-1);
        graphics.drawCenteredString(Minecraft.getInstance().font,label,x+width/2,y+3,0xFFF0E5CE);
    }

    private static boolean isInitialLoadScreen(net.minecraft.client.gui.screens.Screen screen) {
        return screen instanceof ConnectScreen || screen instanceof ReceivingLevelScreen || screen instanceof LevelLoadingScreen;
    }

    private static void beginBrief() {
        briefState = LoadingBriefStore.load();
        currentBrief = LoadingBriefRotation.select(LoadingBriefs.INSTANCE.all(), briefState);
        arrivalPending = false;
    }

    private static void dismissBrief() {
        if (currentBrief == null) return;
        briefState = LoadingBriefRotation.commit(briefState == null ? LoadingBriefStore.load() : briefState,
            currentBrief.id(), LoadingBriefs.INSTANCE.all().size());
        LoadingBriefStore.save(briefState);
        currentBrief = null;
        arrivalPending = false;
    }

    static void renderLoadingBrief(GuiGraphics graphics, LoadingBrief brief, int screenWidth, int screenHeight, boolean arrival) {
        int panelWidth = Math.min(620, screenWidth - 24);
        int panelHeight = Math.min(220, screenHeight - 34);
        int x = (screenWidth - panelWidth) / 2;
        int y = (screenHeight - panelHeight) / 2;
        graphics.fill(x - 2, y - 2, x + panelWidth + 2, y + panelHeight + 2, 0xD0C6A15B);
        graphics.fill(x, y, x + panelWidth, y + panelHeight, 0xED101412);
        int artWidth = Math.min(250, Math.max(112, panelWidth * 2 / 5));
        int artHeight = panelHeight - 24;
        graphics.blit(brief.art(), x + 12, y + 12, artWidth, artHeight, 0, 0, 512, 256, 512, 256);
        int textX = x + artWidth + 26;
        int textWidth = panelWidth - artWidth - 38;
        graphics.drawString(Minecraft.getInstance().font, brief.headline(), textX, y + 22, 0xFFF0E2C5, true);
        int lineY = y + 48;
        for (var line : Minecraft.getInstance().font.split(Component.literal(brief.body()), textWidth)) {
            graphics.drawString(Minecraft.getInstance().font, line, textX, lineY, 0xFFD2C9B5, false);
            lineY += 11;
        }
        if (brief.id().equals("threads")) {
            String binding = OPEN.getTranslatedKeyMessage().getString();
            int keyWidth = Math.max(16, Minecraft.getInstance().font.width(binding) + 8);
            drawKeycap(graphics, binding, textX, Math.min(y + panelHeight - 42, lineY + 12), keyWidth);
            graphics.drawString(Minecraft.getInstance().font, "Threads", textX + keyWidth + 7,
                Math.min(y + panelHeight - 39, lineY + 15), 0xFFE0D4BB, true);
        }
        if (!arrival) graphics.drawCenteredString(Minecraft.getInstance().font, "This lesson will remain after arrival",
            x + panelWidth / 2, y + panelHeight - 14, 0xFF8E9A91);
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
        }

        @SubscribeEvent
        public static void setup(FMLClientSetupEvent event) {
            event.enqueueWork(() -> ItemProperties.register(ThreadRegistry.FACSIMILE.get(),
                new ResourceLocation(BetterContentThreads.MOD_ID, "thread_index"),
                (stack, level, entity, seed) -> ThreadArt.itemIndex(ThreadFacsimileItem.threadId(stack))));
        }
    }
}
