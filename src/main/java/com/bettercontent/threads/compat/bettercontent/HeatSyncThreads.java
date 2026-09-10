package com.bettercontent.threads.compat.bettercontent;

import com.bettercontent.heatsync.api.event.BodyTemperatureEpisodeEvent;
import com.bettercontent.heatsync.api.event.CoolantSafetyEvent;
import com.bettercontent.heatsync.api.event.FoodThermalEpisodeEvent;
import com.bettercontent.threads.ThreadSignals;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/** Maps Heat Sync's authoritative thermal episodes into correlated Thread evidence. */
public final class HeatSyncThreads {
    private HeatSyncThreads() {}

    @SubscribeEvent
    public static void bodyTemperatureChanged(BodyTemperatureEpisodeEvent event) {
        ThreadEvidence evidence = bodyEvidence(event.getStage());
        ThreadSignals.emit(event.getPlayer(), evidence.type(), evidence.value(), event.getEpisodeId());
    }

    @SubscribeEvent
    public static void coolantSafetyChanged(CoolantSafetyEvent event) {
        ThreadEvidence evidence = coolantEvidence(event.getStage());
        ThreadSignals.emit(event.getPlayer(), evidence.type(), evidence.value(), event.getEpisodeId());
    }

    @SubscribeEvent
    public static void foodThermalStateChanged(FoodThermalEpisodeEvent event) {
        ThreadEvidence evidence = foodEvidence(event.getStage());
        ThreadSignals.emit(event.getPlayer(), evidence.type(), evidence.value(), event.getEpisodeId());
    }

    static ThreadEvidence bodyEvidence(BodyTemperatureEpisodeEvent.Stage stage) {
        return switch (stage) {
            case STRESSED_COLD -> new ThreadEvidence("temperature_stress", "cold");
            case STRESSED_HOT -> new ThreadEvidence("temperature_stress", "hot");
            case COMFORT_RESTORED -> new ThreadEvidence("temperature_comfort", "restored");
        };
    }

    static ThreadEvidence coolantEvidence(CoolantSafetyEvent.Stage stage) {
        return switch (stage) {
            case EXCESS_HEAT_OBSERVED -> new ThreadEvidence("heat_excess", "network");
            case SAFE_AFTER_COOLANT_EXCHANGE -> new ThreadEvidence("heat_safe", "safe");
        };
    }

    static ThreadEvidence foodEvidence(FoodThermalEpisodeEvent.Stage stage) {
        return switch (stage) {
            case FROZEN_USE_REJECTED -> new ThreadEvidence("food_thermal_state", "non_neutral");
            case FRESH_USE_FINISHED -> new ThreadEvidence("food_thermal_use", "appropriate");
        };
    }
}
