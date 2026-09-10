package com.bettercontent.threads.compat.bettercontent;

import com.bettercontent.economy.api.event.AuthoredSpiritTradeEvent;
import com.bettercontent.economy.api.event.SpiritAcquiredEvent;
import com.bettercontent.threads.ThreadSignals;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/** Maps Economy's player-kill spirit lifecycle into correlated Thread evidence. */
public final class EconomyThreads {
    private EconomyThreads() {}

    @SubscribeEvent
    public static void spiritAcquired(SpiritAcquiredEvent event) {
        ThreadSignals.emit(event.getPlayer(), "spirit_acquired", event.getSpiritId().toString(),
                event.getSpiritEntityUuid().toString());
    }

    @SubscribeEvent
    public static void authoredSpiritSpent(AuthoredSpiritTradeEvent event) {
        String active = ThreadSignals.activeCorrelation(event.getPlayer(), "coins_do_not_climb");
        if (active != null) {
            ThreadSignals.emit(event.getPlayer(), "authored_trade", "matching_spirit_spent", active);
        }
    }
}
