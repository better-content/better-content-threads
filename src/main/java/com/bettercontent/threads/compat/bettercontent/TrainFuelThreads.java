package com.bettercontent.threads.compat.bettercontent;
import com.bettercontent.createtrainfuelscaling.api.TrainFuelConsumedEvent;
import com.bettercontent.threads.ThreadSignals;
import net.minecraftforge.eventbus.api.SubscribeEvent;
public final class TrainFuelThreads {
    private TrainFuelThreads() {}
    @SubscribeEvent public static void fuelConsumed(TrainFuelConsumedEvent event) {
        if (event.consumed <= event.baseline) return;
        ThreadSignals.emit(event.server, event.operator, "train_fuel", "speed_scaled", event.train + ":" + event.server.getTickCount(),
            "Train consumed " + event.consumed + " fuel ticks at " + Math.round(event.speed) + " blocks/second; baseline " + event.baseline);
    }
}
