package com.bettercontent.learningsurfaces.compat.bettercontent;

import com.bettercontent.settlementroads.api.event.RoadJourneyEvent;
import com.bettercontent.learningsurfaces.ThreadSignals;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/** Maps Settlement Roads' journey lifecycle into correlated Thread evidence. */
public final class SettlementRoadThreads {
    private SettlementRoadThreads() {}

    @SubscribeEvent
    public static void roadJourneyProgressed(RoadJourneyEvent event) {
        ThreadEvidence evidence = evidence(event.getStage());
        ThreadSignals.emit(event.getPlayer(), evidence.type(), evidence.value(), event.getEpisodeId());
    }

    static ThreadEvidence evidence(RoadJourneyEvent.Stage stage) {
        return switch (stage) {
            case ENTERED_WILDERNESS -> new ThreadEvidence("road_enter", "wilderness");
            case ARRIVED_SETTLEMENT -> new ThreadEvidence("road_arrival", "settlement");
        };
    }
}
