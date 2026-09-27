package com.bettercontent.learningsurfaces.compat.bettercontent;
import com.bettercontent.worldlifecyclemanager.api.event.SchematicSubstitutionEvent;
import com.bettercontent.learningsurfaces.ThreadSignals;
import net.minecraftforge.eventbus.api.SubscribeEvent;
public final class SchematicSubstitutionThreads {
 @SubscribeEvent public static void placed(SchematicSubstitutionEvent event){ThreadSignals.emit(event.level.getServer(),event.owner,"schematic_substitute","placed",event.operation.toString(),event.source+" replaced by "+event.target+" at "+event.position.toShortString());}
}
