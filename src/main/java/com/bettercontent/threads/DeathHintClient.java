package com.bettercontent.threads;

import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.DeathScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import java.util.Random;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = BetterContentThreads.MOD_ID, value = Dist.CLIENT)
public final class DeathHintClient {
    private static final Random RANDOM = new Random();
    private static DeathHintSession session = new DeathHintSession();
    private static DeathHintRotation.State history;
    private static DeathScreen active;

    public static void receive(UUID player, String context) {
        var local = Minecraft.getInstance().player;
        if (local != null && local.getUUID().equals(player)) session.receive(context, Util.getMillis());
    }
    @SubscribeEvent public static void logout(ClientPlayerNetworkEvent.LoggingOut event) { reset(); }
    @SubscribeEvent public static void clone(ClientPlayerNetworkEvent.Clone event) { reset(); }
    private static void reset() { active = null; session = new DeathHintSession(); }

    @SubscribeEvent public static void render(ScreenEvent.Render.Post event) {
        if (!(event.getScreen() instanceof DeathScreen death)) return;
        if (active != null && active != death) session = new DeathHintSession();
        active = death;
        if (history == null) history = DeathHintStore.load();
        if (session.selection() == null) session.select(DeathHintRotation.select(DeathHints.INSTANCE.all(),
            session.freeze(Util.getMillis()), history, id -> ModList.get().isLoaded(id), RANDOM));
        if (renderHint(event.getGuiGraphics(), death, session.selection().hint()) && session.record()) {
            history = DeathHintRotation.displayed(history, session.selection());
            DeathHintStore.save(history);
        }
    }

    static Component text(DeathHint hint) {
        var mc = Minecraft.getInstance();
        return Component.literal(hint.text().replace("{sneak}", mc.options.keyShift.getTranslatedKeyMessage().getString())
            .replace("{use}", mc.options.keyUse.getTranslatedKeyMessage().getString())
            .replace("{threads}", ThreadClient.OPEN.getTranslatedKeyMessage().getString()));
    }

    /** Also used by the isolated visual fixture; layout measures actual native widgets. */
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
