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

    public static void emit(ServerPlayer player,String type,String value,String correlation,String context){com.bettercontent.threads.ThreadSignals.emit(player,type,value,correlation,context);}
    public static void emit(net.minecraft.server.MinecraftServer server,java.util.UUID player,String type,String value,String correlation,String context){com.bettercontent.threads.ThreadSignals.emit(server,player,type,value,correlation,context);}
}
