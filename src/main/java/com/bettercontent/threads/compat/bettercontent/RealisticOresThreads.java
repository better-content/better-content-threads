package com.bettercontent.threads.compat.bettercontent;

import com.bettercontent.realisticores.api.event.DepositSurveyEvent;
import com.bettercontent.threads.ThreadSignals;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/** Maps Realistic Ores' persisted sample-to-extraction survey episodes into Thread evidence. */
public final class RealisticOresThreads {
    private RealisticOresThreads() {}

    @SubscribeEvent
    public static void depositSurveyProgressed(DepositSurveyEvent event) {
        ThreadEvidence evidence = evidence(event.kind(), event.family());
        ThreadSignals.emit(event.player(), evidence.type(), evidence.value(), event.episodeId());
    }

    static ThreadEvidence evidence(DepositSurveyEvent.Kind kind, String family) {
        return switch (kind) {
            case SAMPLE_READ -> new ThreadEvidence("deposit_read", family);
            case DEPOSIT_EXTRACTED -> new ThreadEvidence("deposit_extract", family);
        };
    }
}
