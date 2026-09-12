package com.bettercontent.threads;

import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.TitleScreen;
import org.lwjgl.glfw.GLFW;
import java.util.Random;
import java.util.Set;
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
    private record Frame(String name, int width, int scale, Supplier<Screen> screen) {}
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
            for (int[] size : new int[][]{{960,3},{1280,3},{1280,2}}) {
                int width=size[0],scale=size[1];String suffix=width+"-scale-"+scale;
                frames.add(new Frame("menu-native-"+suffix,width,scale,()->new TitleScreen(false)));
                frames.add(new Frame("pause-native-"+suffix,width,scale,()->new PauseScreen(true)));
                for(String context:List.of("door_locked","injury_cure","sugar_crash")) {
                    var selected=DeathHintRotation.select(DeathHints.INSTANCE.all(),context,"pause",DeathHintRotation.State.empty(),id->true,new Random(38),Set.of());
                    if(!selected.hint().contexts().contains(context))throw new IllegalStateException("Context fixture selected unrelated advice: "+context);
                    frames.add(new Frame("pause-context-"+context+"-"+suffix,width,scale,()->new PauseHintVisualScreen(selected.hint(),true)));
                }
            }
            if(Boolean.getBoolean("bc.learningVisual.menuOnly"))frames.removeIf(f->!f.name().startsWith("menu-native-"));
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
        GLFW.glfwSetWindowSize(mc.getWindow().getWindow(),next.width(),720);
        mc.options.guiScale().set(next.scale());
        mc.resizeDisplay();
        mc.setScreen(next.screen().get());
        ticks = 0;
        capturing = false;
    }
}
