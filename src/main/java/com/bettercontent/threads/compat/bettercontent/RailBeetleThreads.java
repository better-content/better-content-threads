package com.bettercontent.threads.compat.bettercontent;
import com.bettercontent.railbeetle.api.BeetleWorkEvent;
import com.bettercontent.threads.ThreadSignals;
import net.minecraftforge.eventbus.api.SubscribeEvent;
public final class RailBeetleThreads {
    private RailBeetleThreads() {}
    @SubscribeEvent public static void worked(BeetleWorkEvent event) {
        String type = event.kind == BeetleWorkEvent.Kind.ROUTE_FINISHED ? "rail_beetle" : "beetle_engine";
        String value = event.kind == BeetleWorkEvent.Kind.ROUTE_FINISHED ? "route_complete" : "magic_work";
        ThreadSignals.emit(event.level.getServer(), event.operator, type, value, event.operation.toString(), "Rail Beetle: " + event.engine);
    }
}
