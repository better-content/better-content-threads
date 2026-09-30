package com.bettercontent.betterdiscoveryguides.compat.bettercontent;
import com.bettercontent.betterbumblezonecrops.api.event.CultivarHarvestEvent;
import com.bettercontent.betterbumblezonecrops.api.event.WildVegetableDropEvent;
import com.bettercontent.betterdiscoveryguides.ThreadSignals;
import net.minecraftforge.eventbus.api.SubscribeEvent;
public final class CultivarThreads {
 @SubscribeEvent public static void harvest(CultivarHarvestEvent event){ThreadSignals.emit(event.level.getServer(),event.owner,"cultivar_harvest","overworld",event.planting.toString(),event.cultivar+" at "+event.position.toShortString());}
 @SubscribeEvent public static void wildVegetable(WildVegetableDropEvent event){ThreadSignals.emit(event.player,"wild_vegetable_seedless","first",event.player.getUUID()+":wild:"+event.position.asLong());}
}
