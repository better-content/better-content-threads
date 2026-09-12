package com.bettercontent.threads;

import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/** Isolated tips-only native client review; never ships and injects no pointer input. */
@Mod.EventBusSubscriber(modid = BetterContentThreads.MOD_ID, value = Dist.CLIENT)
public final class TipVisualReview {
    private record Frame(String name, int scale, Supplier<Screen> screen) {}
    private static final List<Frame> frames = new ArrayList<>();
    private static int frame = -1;
    private static int ticks;
    private static boolean capturing;

    @SubscribeEvent public static void tick(TickEvent.ClientTickEvent event) {
        if (!Boolean.getBoolean("bc.learningVisual.tipsOnly") || event.phase != TickEvent.Phase.END) return;
        var mc = Minecraft.getInstance();
        if (mc.getOverlay() != null || mc.screen == null || capturing) return;
        if (frame < 0) {
            if (++ticks < 30) return;
            for (int scale : new int[]{4, 3, 2}) {
                frames.add(new Frame("pause-native-" + scale, scale, () -> new PauseScreen(true)));
                for (String id : List.of("combat_space", "beetle_route_beacons", "revive_use")) {
                    var hint = DeathHints.INSTANCE.all().stream().filter(h -> h.id().equals(id)).findFirst().orElseThrow();
                    frames.add(new Frame("pause-single-" + id + "-" + scale, scale, () -> new PauseHintVisualScreen(hint, true)));
                    frames.add(new Frame("pause-multi-" + id + "-" + scale, scale, () -> new PauseHintVisualScreen(hint, false)));
                }
            }
            next();
            return;
        }
        if (++ticks < 12) return;
        capturing = true;
        Screenshot.grab(mc.gameDirectory, frames.get(frame).name() + ".png", mc.getMainRenderTarget(), message -> {
            System.out.println("TIP_VISUAL captured " + frames.get(frame).name());
            mc.execute(TipVisualReview::next);
        });
    }
    private static void next() {
        var mc = Minecraft.getInstance();
        if (++frame >= frames.size()) {
            System.out.println("TIP_VISUAL complete frames=" + frames.size());
            mc.stop();
            return;
        }
        var next = frames.get(frame);
        mc.options.guiScale().set(next.scale());
        mc.resizeDisplay();
        mc.setScreen(next.screen().get());
        ticks = 0;
        capturing = false;
    }
}
