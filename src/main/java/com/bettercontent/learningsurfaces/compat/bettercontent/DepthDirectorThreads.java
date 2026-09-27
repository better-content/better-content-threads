package com.bettercontent.learningsurfaces.compat.bettercontent;
import com.bettercontent.depthdirector.api.event.CaveWarningEvent;
import com.bettercontent.depthdirector.api.event.CavePressureStartedEvent;
import com.bettercontent.learningsurfaces.ThreadSignals;
import net.minecraftforge.eventbus.api.SubscribeEvent;
public final class DepthDirectorThreads {
 @SubscribeEvent public static void warning(CaveWarningEvent event){ThreadSignals.emit(event.player,"depth_warning","committed",event.encounter.toString(),"Approach "+event.approach.toShortString());}
 @SubscribeEvent public static void pressureStarted(CavePressureStartedEvent event){ThreadSignals.emit(event.player,"depth_pressure","started",event.player.getUUID()+":pressure:"+event.tick);}
}
