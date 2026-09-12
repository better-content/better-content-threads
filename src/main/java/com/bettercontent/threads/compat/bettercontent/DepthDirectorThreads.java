package com.bettercontent.threads.compat.bettercontent;
import com.bettercontent.depthdirector.api.event.CaveWarningEvent;
import com.bettercontent.threads.ThreadSignals;
import net.minecraftforge.eventbus.api.SubscribeEvent;
public final class DepthDirectorThreads {
 @SubscribeEvent public static void warning(CaveWarningEvent event){ThreadSignals.emit(event.player,"depth_warning","committed",event.encounter.toString(),"Approach "+event.approach.toShortString());}
}
