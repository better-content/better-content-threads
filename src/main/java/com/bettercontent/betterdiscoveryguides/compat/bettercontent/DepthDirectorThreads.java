package com.bettercontent.betterdiscoveryguides.compat.bettercontent;
import com.bettercontent.bettercaveencounters.api.event.CaveWarningEvent;
import com.bettercontent.bettercaveencounters.api.event.CavePressureStartedEvent;
import com.bettercontent.betterdiscoveryguides.ThreadSignals;
import net.minecraftforge.eventbus.api.SubscribeEvent;
public final class DepthDirectorThreads {
 @SubscribeEvent public static void warning(CaveWarningEvent event){ThreadSignals.emit(event.player,"depth_warning","committed",event.encounter.toString(),"Approach "+event.approach.toShortString());}
 @SubscribeEvent public static void pressureStarted(CavePressureStartedEvent event){ThreadSignals.emit(event.player,"depth_pressure","started",event.player.getUUID()+":pressure:"+event.tick);}
}
