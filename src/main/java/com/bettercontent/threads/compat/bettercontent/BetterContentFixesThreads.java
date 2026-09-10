package com.bettercontent.threads.compat.bettercontent;

import com.bettercontent.bettercontentfixes.api.event.CustomControlEpisodeEvent;
import com.bettercontent.bettercontentfixes.api.event.SleepTimelapseEvent;
import com.bettercontent.threads.ThreadSignals;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/** Maps Better Content Fixes' control and sleep episodes into correlated Thread evidence. */
public final class BetterContentFixesThreads {
    private BetterContentFixesThreads() {}

    @SubscribeEvent
    public static void customControlProgressed(CustomControlEpisodeEvent event) {
        ThreadEvidence evidence = controlEvidence(event.kind(), event.acceptedActionId());
        ThreadSignals.emit(event.player(), evidence.type(), evidence.value(), event.episodeId());
    }

    @SubscribeEvent
    public static void sleepTimelapseProgressed(SleepTimelapseEvent event) {
        ThreadEvidence evidence = sleepEvidence(event.kind());
        ThreadSignals.emit(event.player(), evidence.type(), evidence.value(), event.episodeId());
    }

    static ThreadEvidence controlEvidence(CustomControlEpisodeEvent.Kind kind, String acceptedActionId) {
        return switch (kind) {
            case FIRST_ACCEPTED -> new ThreadEvidence("custom_control_used", acceptedActionId);
            case DISTINCT_SECOND -> new ThreadEvidence("custom_control_mastered", "distinct_second");
        };
    }

    static ThreadEvidence sleepEvidence(SleepTimelapseEvent.Kind kind) {
        return switch (kind) {
            case STARTED -> new ThreadEvidence("sleep_started", "night");
            case FINISHED -> new ThreadEvidence("sleep_finished", "simulated_time");
        };
    }
}
