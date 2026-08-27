package com.bettercontent.threads.api;

import net.minecraft.server.level.ServerPlayer;

/** Stable optional integration surface for completed native actions. */
public final class ThreadSignals {
    private ThreadSignals() {}

    public static void emit(ServerPlayer player, String type, String value) {
        com.bettercontent.threads.ThreadSignals.emit(player, type, value);
    }
}
