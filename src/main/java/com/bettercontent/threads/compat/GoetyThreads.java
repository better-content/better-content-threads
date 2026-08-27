package com.bettercontent.threads.compat;

import com.Polarice3.Goety.common.events.spell.CastMagicEvent;
import com.Polarice3.Goety.common.events.spell.ChangeSoulEnergyEvent;
import com.bettercontent.threads.ThreadSignals;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/** Goety soul acquisition followed by an actual spell cast. */
public final class GoetyThreads {
    private GoetyThreads() {}

    @SubscribeEvent
    public static void soulGained(ChangeSoulEnergyEvent.Gain event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || event.getSoulChange() <= 0) return;
        String token = player.getUUID() + ":goety:" + player.server.getTickCount();
        ThreadSignals.emit(player, "soul_resource", "goety", token);
    }

    @SubscribeEvent
    public static void soulWorked(CastMagicEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || event.getSpell() == null) return;
        String token = ThreadSignals.activeCorrelation(player, "dead_leave_work");
        if (token != null) ThreadSignals.emit(player, "soul_work", "goety", token);
    }
}
