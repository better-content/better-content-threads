package com.bettercontent.threads.compat.bettercontent;

import com.bettercontent.arcanechunkloaders.api.event.ArcaneAnchorProgressEvent;
import com.bettercontent.bettercontentfixes.api.event.CustomControlEpisodeEvent;
import com.bettercontent.bettercontentfixes.api.event.SleepTimelapseEvent;
import com.bettercontent.heatsync.api.event.BodyTemperatureEpisodeEvent;
import com.bettercontent.heatsync.api.event.FoodThermalEpisodeEvent;
import com.bettercontent.playertraces.api.event.TraceEpisodeEvent;
import com.bettercontent.rpgstats.api.event.LifeAllocationEvent;
import com.bettercontent.realisticores.api.event.DepositSurveyEvent;
import com.bettercontent.settlementroads.api.event.RoadJourneyEvent;
import com.bettercontent.systemicsalience.api.event.NutritionEpisodeEvent;
import com.bettercontent.watersurvival.api.event.WaterSafetyEvent;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class BetterContentEventMappingTest {
    @Test
    void mapsEveryThermalStage() {
        assertEquals(evidence("temperature_stress", "cold"),
            HeatSyncThreads.bodyEvidence(BodyTemperatureEpisodeEvent.Stage.STRESSED_COLD));
        assertEquals(evidence("temperature_stress", "hot"),
            HeatSyncThreads.bodyEvidence(BodyTemperatureEpisodeEvent.Stage.STRESSED_HOT));
        assertEquals(evidence("temperature_comfort", "restored"),
            HeatSyncThreads.bodyEvidence(BodyTemperatureEpisodeEvent.Stage.COMFORT_RESTORED));
        assertEquals(evidence("food_thermal_state", "frozen_use_rejected"),
            HeatSyncThreads.foodEvidence(FoodThermalEpisodeEvent.Stage.FROZEN_USE_REJECTED));
        assertEquals(evidence("food_thermal_use", "appropriate"),
            HeatSyncThreads.foodEvidence(FoodThermalEpisodeEvent.Stage.FRESH_USE_FINISHED));
    }

    @Test
    void mapsEveryJourneyAndPlayerStateStage() {
        assertEquals(evidence("road_enter", "wilderness"),
            SettlementRoadThreads.evidence(RoadJourneyEvent.Stage.ENTERED_WILDERNESS));
        assertEquals(evidence("road_arrival", "settlement"),
            SettlementRoadThreads.evidence(RoadJourneyEvent.Stage.ARRIVED_SETTLEMENT));
        assertEquals(evidence("trace_commit", "footprint"),
            PlayerTraceThreads.evidence(TraceEpisodeEvent.Kind.COMMITTED));
        assertEquals(evidence("trace_return", "own_old_trace"),
            PlayerTraceThreads.evidence(TraceEpisodeEvent.Kind.RETURNED));
        assertEquals(evidence("life_allocation", "available"),
            RpgStatsThreads.evidence(LifeAllocationEvent.State.AVAILABLE));
        assertEquals(evidence("life_allocation", "spent"),
            RpgStatsThreads.evidence(LifeAllocationEvent.State.SPENT));
        assertEquals(evidence("deposit_read", "ironstone"),
            RealisticOresThreads.evidence(DepositSurveyEvent.Kind.SAMPLE_READ, "ironstone"));
        assertEquals(evidence("deposit_extract", "ironstone"),
            RealisticOresThreads.evidence(DepositSurveyEvent.Kind.DEPOSIT_EXTRACTED, "ironstone"));
    }

    @Test
    void mapsEveryControlSleepNutritionAndWaterStage() {
        assertEquals(evidence("custom_control_used", "parcool:cat_leap"),
            BetterContentFixesThreads.controlEvidence(CustomControlEpisodeEvent.Kind.FIRST_ACCEPTED, "parcool:cat_leap"));
        assertEquals(evidence("custom_control_mastered", "distinct_second"),
            BetterContentFixesThreads.controlEvidence(CustomControlEpisodeEvent.Kind.DISTINCT_SECOND, "ignored"));
        assertEquals(evidence("sleep_started", "night"),
            BetterContentFixesThreads.sleepEvidence(SleepTimelapseEvent.Kind.STARTED));
        assertEquals(evidence("sleep_finished", "simulated_time"),
            BetterContentFixesThreads.sleepEvidence(SleepTimelapseEvent.Kind.FINISHED));
        assertEquals(evidence("nutrition_warning", "hunger_full_nutrition_low"),
            SystemicSalienceThreads.evidence(NutritionEpisodeEvent.Kind.WARNING));
        assertEquals(evidence("nutrition_recovered", "balanced_meal"),
            SystemicSalienceThreads.evidence(NutritionEpisodeEvent.Kind.RECOVERED));
        assertEquals(evidence("life_allocation", "lost_on_death"),
            RpgStatsThreads.evidence(LifeAllocationEvent.State.LOST_ON_DEATH));
    }

    private static ThreadEvidence evidence(String type, String value) {
        return new ThreadEvidence(type, value);
    }
}
