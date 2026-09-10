package com.bettercontent.threads.compat.bettercontent;

import com.bettercontent.playertraces.api.event.TraceEpisodeEvent;
import com.bettercontent.threads.ThreadSignals;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/** Maps Player Traces' journey episodes into correlated Thread evidence. */
public final class PlayerTraceThreads {
    private PlayerTraceThreads() {}

    @SubscribeEvent
    public static void traceProgressed(TraceEpisodeEvent event) {
        ThreadEvidence evidence = evidence(event.getKind());
        ThreadSignals.emit(event.getPlayer(), evidence.type(), evidence.value(), event.getEpisodeId());
    }

    static ThreadEvidence evidence(TraceEpisodeEvent.Kind kind) {
        return switch (kind) {
            case COMMITTED -> new ThreadEvidence("trace_commit", "footprint");
            case RETURNED -> new ThreadEvidence("trace_return", "own_old_trace");
        };
    }
}
