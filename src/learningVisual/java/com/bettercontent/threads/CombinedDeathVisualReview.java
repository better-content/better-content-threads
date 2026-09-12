package com.bettercontent.threads;

import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;
import java.util.List;

/** Isolated combined layout review. No world, damage, or inventory changes. */
@Mod.EventBusSubscriber(modid=BetterContentThreads.MOD_ID,value=Dist.CLIENT)
public final class CombinedDeathVisualReview {
    private record Frame(int width,int height,int scale,String hint){}
    private static final List<Frame> FRAMES=List.of(new Frame(960,720,3,"door_prepare"),new Frame(1280,720,3,"injury_treatment_prepare"),new Frame(1280,720,2,"door_prepare"));
    private static int index=-1,ticks;
    private static boolean capturing;
    @SubscribeEvent public static void tick(TickEvent.ClientTickEvent event){
        if(!Boolean.getBoolean("bc.learningVisual.combinedDeathOnly")||event.phase!=TickEvent.Phase.END)return;
        var mc=Minecraft.getInstance();if(mc.getOverlay()!=null||mc.screen==null)return;
        if(index<0){if(++ticks<30)return;if(!ModList.get().isLoaded("downed_player_revival"))throw new IllegalStateException("Combined review requires actual injury runtime");next();return;}
        if(capturing||++ticks<25)return;
        capturing=true;
        var frame=FRAMES.get(index);String name="combined-death-"+mc.screen.width+"x"+mc.screen.height;
        var bounds=com.bettercontent.downedplayerrevival.client.DeathRecapOverlay.reservedBounds(mc.screen.width,mc.screen.height);
        if(bounds.height()<48||bounds.y()+bounds.height()>mc.screen.height-30)throw new IllegalStateException("Recap does not fit "+name);
        Screenshot.grab(mc.gameDirectory,name+".png",mc.getMainRenderTarget(),message->{System.out.println("COMBINED_DEATH_VISUAL "+name+" "+message.getString());mc.execute(CombinedDeathVisualReview::next);});
    }
    private static void next(){
        var mc=Minecraft.getInstance();if(++index>=FRAMES.size()){System.out.println("COMBINED_DEATH_VISUAL complete");mc.stop();return;}
        var frame=FRAMES.get(index);GLFW.glfwSetWindowSize(mc.getWindow().getWindow(),frame.width(),frame.height());
        mc.options.guiScale().set(frame.scale());mc.resizeDisplay();
        var hint=DeathHints.INSTANCE.all().stream().filter(h->h.id().equals(frame.hint())).findFirst().orElseThrow();mc.setScreen(new CombinedDeathVisualScreen(hint));
        ticks=0;capturing=false;
    }
}
