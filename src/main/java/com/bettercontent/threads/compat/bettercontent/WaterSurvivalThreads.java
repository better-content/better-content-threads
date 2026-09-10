package com.bettercontent.threads.compat.bettercontent;

import com.bettercontent.threads.ThreadSignals;
import com.bettercontent.watersurvival.api.event.WaterSafetyEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/** Maps Water Survival's unsafe-water lifecycle into correlated Thread evidence. */
public final class WaterSurvivalThreads {
    private WaterSurvivalThreads() {}

    @SubscribeEvent
    public static void waterSafetyChanged(WaterSafetyEvent event) {
        ThreadEvidence evidence = evidence(event.getStage());
        switch (event.getStage()) {
            case THIRST_LOST -> {
                if (!event.getEpisodeId().isBlank()) {
                    ThreadSignals.emit(event.getPlayer(), evidence.type(), evidence.value(), event.getEpisodeId());
                }
            }
            case PURIFIED_WATER_CONSUMED -> {
                String token = event.getEpisodeId().isBlank()
                    ? ThreadSignals.activeCorrelation(event.getPlayer(), "water_made_safe")
                    : event.getEpisodeId();
                if (token != null) {
                    ThreadSignals.emit(event.getPlayer(), evidence.type(), evidence.value(), token);
                }
            }
        }
    }

    static ThreadEvidence evidence(WaterSafetyEvent.Stage stage) {
        return switch (stage) {
            case THIRST_LOST -> new ThreadEvidence("thirst_loss", "first_drop");
            case PURIFIED_WATER_CONSUMED -> new ThreadEvidence("water_drink", "purity_3_correlated");
        };
    }
}
