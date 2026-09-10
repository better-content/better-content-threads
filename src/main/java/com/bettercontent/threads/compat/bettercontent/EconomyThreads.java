package com.bettercontent.threads.compat.bettercontent;

import com.bettercontent.economy.api.event.AuthoredCoinTradeEvent;
import com.bettercontent.economy.api.event.CoinAcquiredEvent;
import com.bettercontent.threads.ThreadSignals;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/** Maps Economy's authoritative coin lifecycle into correlated Thread evidence. */
public final class EconomyThreads {
    private EconomyThreads() {}

    @SubscribeEvent
    public static void coinAcquired(CoinAcquiredEvent event) {
        ThreadSignals.emit(event.getPlayer(), "coin_acquired", event.getDenomination().toString(), event.getEpisodeId());
    }

    @SubscribeEvent
    public static void authoredCoinSpent(AuthoredCoinTradeEvent event) {
        String active = ThreadSignals.activeCorrelation(event.getPlayer(), "coins_do_not_climb");
        if (active != null) {
            ThreadSignals.emit(event.getPlayer(), "authored_trade", "coin_spent", active);
        }
    }
}
