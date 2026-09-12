package com.bettercontent.threads.compat.bettercontent;

import com.bettercontent.systemicsalience.api.event.MetabolicDiscoveryEvent;
import com.bettercontent.threads.ThreadSignals;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public final class MetabolicDiscoveryThreads {
    @SubscribeEvent public static void committed(MetabolicDiscoveryEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        String type = switch (event.kind()) {
            case SUGAR_CRASH -> "sugar_crash";
            case HEAVY_BLOW -> "heavy_blow";
            case WORK_RHYTHM -> "work_rhythm";
            case DEEP_RESERVE -> "deep_reserve";
            case CLEANSE -> "nutrition_cleanse";
        };
        String value = switch (event.kind()) {
            case SUGAR_CRASH -> "entered";
            case HEAVY_BLOW -> "executed";
            case WORK_RHYTHM -> "maximum";
            case DEEP_RESERVE -> "activated";
            case CLEANSE -> "removed";
        };
        ThreadSignals.emit(player, type, value, event.episode(), event.detail());
    }
}
