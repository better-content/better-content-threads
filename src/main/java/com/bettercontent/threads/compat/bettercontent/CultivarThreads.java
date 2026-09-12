package com.bettercontent.threads.compat.bettercontent;
import com.bettercontent.bumblezonecultivars.api.event.CultivarHarvestEvent;
import com.bettercontent.threads.ThreadSignals;
import net.minecraftforge.eventbus.api.SubscribeEvent;
public final class CultivarThreads {
 @SubscribeEvent public static void harvest(CultivarHarvestEvent event){ThreadSignals.emit(event.level.getServer(),event.owner,"cultivar_harvest","overworld",event.planting.toString(),event.cultivar+" at "+event.position.toShortString());}
}
