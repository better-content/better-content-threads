package com.bettercontent.threads.compat.bettercontent;
import com.bettercontent.tinkersconstructaffixes.api.event.RuinousFluxEvent;
import com.bettercontent.threads.ThreadSignals;
import net.minecraftforge.eventbus.api.SubscribeEvent;
public final class RuinousFluxThreads {
 @SubscribeEvent public static void mutated(RuinousFluxEvent event){ThreadSignals.emit(event.getPlayer(),"ruinous_flux","resolved",event.getPlayer().getUUID()+":mutation:"+event.getOperation(),event.getPart()+": "+(event.getDestroyed()?"destroyed":"permanently mutated"));}
}
