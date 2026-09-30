package com.bettercontent.betterdiscoveryguides.compat.bettercontent;
import com.bettercontent.betterrailbeetle.api.BeetleWorkEvent;
import com.bettercontent.betterdiscoveryguides.ThreadSignals;
import net.minecraftforge.eventbus.api.SubscribeEvent;
public final class RailBeetleThreads {
    private RailBeetleThreads() {}
    @SubscribeEvent public static void worked(BeetleWorkEvent event) {
        String type = event.kind == BeetleWorkEvent.Kind.ROUTE_FINISHED ? "better_rail_beetle" : "beetle_engine";
        String value = event.kind == BeetleWorkEvent.Kind.ROUTE_FINISHED ? "route_complete" : "magic_work";
        ThreadSignals.emit(event.level.getServer(), event.operator, type, value, event.operation.toString(), "Rail Beetle: " + event.engine);
    }
}
