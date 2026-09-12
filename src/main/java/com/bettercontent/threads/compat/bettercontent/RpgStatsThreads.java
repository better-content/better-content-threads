package com.bettercontent.threads.compat.bettercontent;

import com.bettercontent.rpgstats.api.event.LifeAllocationEvent;
import com.bettercontent.threads.ThreadSignals;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/** Maps RPG Stats' allocation lifecycle into correlated Thread evidence. */
public final class RpgStatsThreads {
    private RpgStatsThreads() {}

    @SubscribeEvent
    public static void lifeAllocated(LifeAllocationEvent event) {
        ThreadEvidence evidence = evidence(event.getState());
        ThreadSignals.emit(event.getPlayer(), evidence.type(), evidence.value(), event.getEpisodeId());
    }

    static ThreadEvidence evidence(LifeAllocationEvent.State state) {
        return new ThreadEvidence("life_allocation", switch (state) {
            case AVAILABLE -> "available";
            case SPENT -> "spent";
            case LOST_ON_DEATH -> "lost_on_death";
        });
    }
}
