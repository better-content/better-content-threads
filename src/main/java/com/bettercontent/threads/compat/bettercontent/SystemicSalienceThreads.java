package com.bettercontent.threads.compat.bettercontent;

import com.bettercontent.systemicsalience.api.event.NutritionEpisodeEvent;
import com.bettercontent.threads.ThreadSignals;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/** Maps Systemic Salience's nutrition episodes into correlated Thread evidence. */
public final class SystemicSalienceThreads {
    private SystemicSalienceThreads() {}

    @SubscribeEvent
    public static void nutritionChanged(NutritionEpisodeEvent event) {
        ThreadEvidence evidence = evidence(event.kind());
        ThreadSignals.emit(event.player(), evidence.type(), evidence.value(), event.episodeId());
    }

    static ThreadEvidence evidence(NutritionEpisodeEvent.Kind kind) {
        return switch (kind) {
            case WARNING -> new ThreadEvidence("nutrition_warning", "hunger_full_nutrition_low");
            case RECOVERED -> new ThreadEvidence("nutrition_recovered", "balanced_meal");
        };
    }
}
