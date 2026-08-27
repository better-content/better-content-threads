package com.bettercontent.threads.api;

import net.minecraft.server.level.ServerPlayer;

/** Stable optional integration surface for completed native actions. */
public final class ThreadSignals {
    private ThreadSignals() {}

    public static void emit(ServerPlayer player, String type, String value) {
        com.bettercontent.threads.ThreadSignals.emit(player, type, value);
    }

    /** Emits native evidence belonging to one bounded gameplay episode. */
    public static void emit(ServerPlayer player, String type, String value, String correlationToken) {
        com.bettercontent.threads.ThreadSignals.emit(player, type, value, correlationToken);
    }

    /** Returns the active episode token for an optional producer that must resume after reload. */
    public static String activeCorrelation(ServerPlayer player, String threadId) {
        return com.bettercontent.threads.ThreadSignals.activeCorrelation(player, threadId);
    }
}
