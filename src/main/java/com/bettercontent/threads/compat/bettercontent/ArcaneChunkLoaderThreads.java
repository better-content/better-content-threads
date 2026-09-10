package com.bettercontent.threads.compat.bettercontent;

import com.bettercontent.arcanechunkloaders.api.event.ArcaneAnchorProgressEvent;
import com.bettercontent.threads.ThreadSignals;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/** Maps Arcane Chunk Loaders' anchor lifecycle into correlated Thread evidence. */
public final class ArcaneChunkLoaderThreads {
    private ArcaneChunkLoaderThreads() {}

    @SubscribeEvent
    public static void anchorProgressed(ArcaneAnchorProgressEvent event) {
        ThreadEvidence evidence = evidence(event.getStage());
        ThreadSignals.emit(event.getPlayer(), evidence.type(), evidence.value(), event.getAnchorId().toString());
    }

    static ThreadEvidence evidence(ArcaneAnchorProgressEvent.Stage stage) {
        return new ThreadEvidence("arcane_anchor", switch (stage) {
            case PLACED -> "placed";
            case REMOTE_TICKET_VERIFIED -> "ticket_verified";
        });
    }
}
