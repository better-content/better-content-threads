package com.bettercontent.threads;

import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.screens.DeathScreen;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import java.util.List;
import java.util.Random;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = BetterContentThreads.MOD_ID, value = Dist.CLIENT)
public final class DeathHintClient {
    private static final Random RANDOM = new Random();
    private static HintLifecycle lifecycle;
    private static Screen layoutScreen;
    private static List<WidgetPosition> positions = List.of();
    private record WidgetPosition(AbstractWidget widget, int y) {}

    private static HintLifecycle lifecycle() {
        if (lifecycle == null) lifecycle = new HintLifecycle(DeathHintStore.load());
        return lifecycle;
    }
    public static void receive(UUID player, String context) {
        var local = Minecraft.getInstance().player;
        if (local != null && local.getUUID().equals(player) && lifecycle().death(context, Util.getMillis()))
            DeathHintStore.save(lifecycle().state());
    }
    @SubscribeEvent public static void logout(ClientPlayerNetworkEvent.LoggingOut event) {
        if (lifecycle != null) lifecycle.disconnect();
        layoutScreen = null;
        positions = List.of();
    }
    @SubscribeEvent public static void clone(ClientPlayerNetworkEvent.Clone event) {
        if (lifecycle != null) lifecycle.respawn();
    }
    private static boolean normalPause(Screen screen) {
        return screen instanceof PauseScreen && screen.children().stream().filter(c -> c instanceof Button).count() > 1;
    }
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void init(ScreenEvent.Init.Post event) {
        if (normalPause(event.getScreen())) rememberPauseLayout(event.getScreen());
    }
    @SubscribeEvent public static void beforeRender(ScreenEvent.Render.Pre event) {
        if (normalPause(event.getScreen())) {
            var selected = lifecycle().pauseTip(DeathHints.INSTANCE.all(), id -> ModList.get().isLoaded(id), RANDOM);
            if (selected != null) layoutPause(event.getScreen(), selected.hint());
        }
    }
    @SubscribeEvent public static void render(ScreenEvent.Render.Post event) {
        var screen = event.getScreen();
        boolean pause = normalPause(screen);
        if (!pause && !(screen instanceof DeathScreen)) return;
        var before = lifecycle().state();
        var selected = pause
            ? lifecycle().pauseTip(DeathHints.INSTANCE.all(), id -> ModList.get().isLoaded(id), RANDOM)
            : lifecycle().deathTip(DeathHints.INSTANCE.all(), id -> ModList.get().isLoaded(id), RANDOM, Util.getMillis());
        if (selected != null && renderHint(event.getGuiGraphics(), screen, selected.hint())) lifecycle().displayed(selected, pause);
        if (!before.equals(lifecycle().state())) DeathHintStore.save(lifecycle().state());
    }

    static Component text(DeathHint hint) {
        var mc = Minecraft.getInstance();
        return Component.literal(hint.text().replace("{sneak}", mc.options.keyShift.getTranslatedKeyMessage().getString())
            .replace("{use}", mc.options.keyUse.getTranslatedKeyMessage().getString())
            .replace("{threads}", ThreadClient.OPEN.getTranslatedKeyMessage().getString()));
    }
    static void rememberPauseLayout(Screen screen) {
        layoutScreen = screen;
        positions = screen.children().stream().filter(c -> c instanceof AbstractWidget)
            .map(c -> (AbstractWidget) c).map(w -> new WidgetPosition(w, w.getY())).toList();
    }
    static void layoutPause(Screen screen, DeathHint hint) {
        if (layoutScreen != screen) rememberPauseLayout(screen);
        var controls = positions.stream().filter(p -> p.widget() instanceof Button && p.y() >= 28).toList();
        if (controls.isEmpty()) return;
        int first = controls.stream().mapToInt(WidgetPosition::y).min().orElse(36);
        int bottom = controls.stream().mapToInt(p -> p.y() + p.widget().getHeight()).max().orElse(36);
        int lines = Minecraft.getInstance().font.split(text(hint), DeathHintLayout.textWidth(screen.width)).size();
        var layout = PauseHintLayout.calculate(screen.height, first, bottom, lines, Minecraft.getInstance().font.lineHeight);
        for (var p : positions) {
            int y = p.y();
            if (layout.fits()) {
                if (p.widget() instanceof StringWidget && layout.titleY() >= 0) y = layout.titleY();
                else if (p.widget() instanceof Button && p.y() >= 28) y -= layout.shift();
            }
            p.widget().setY(y);
        }
    }

    /** Layout measures native widgets and is shared by the isolated visual fixtures. */
    static boolean renderHint(GuiGraphics graphics, Screen screen, DeathHint hint) {
        var font = Minecraft.getInstance().font;
        var lines = font.split(text(hint), DeathHintLayout.textWidth(screen.width));
        int bottom = screen.children().stream().filter(c -> c instanceof AbstractWidget)
            .map(c -> (AbstractWidget) c).filter(w -> w.visible).mapToInt(w -> w.getY() + w.getHeight())
            .max().orElse(screen.height / 4 + 116);
        var layout = DeathHintLayout.calculate(screen.width, screen.height, bottom, lines.size(), font.lineHeight);
        if (!layout.visible()) return false;
        graphics.fill(layout.x(), layout.y(), layout.x() + layout.width(), layout.y() + layout.height(), 0xCE151310);
        graphics.drawString(font, Component.translatable("screen.better_content_threads.death_hint"),
            layout.x() + 8, layout.y() + 6, 0xC6A15B, false);
        int y = layout.y() + 9 + font.lineHeight;
        for (var line : lines) {
            graphics.drawString(font, line, layout.x() + 8, y, 0xEEE8DB, false);
            y += font.lineHeight;
        }
        return true;
    }
}
