package com.bettercontent.threads.compat.bettercontent;

import com.bettercontent.downedplayerrevival.api.event.PlayerDownedEvent;
import com.bettercontent.threads.ThreadSignals;
import com.bettercontent.threads.DeathHintEvents;
import com.bettercontent.downedplayerrevival.api.event.PlayerRevivedEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/** Maps Revival's published downed lifecycle into correlated Thread evidence. */
public final class DownedRevivalThreads {
    private DownedRevivalThreads() {}

    @SubscribeEvent
    public static void revived(PlayerRevivedEvent event) {
        DeathHintEvents.CONTEXT.clear(event.getEntity().getUUID());
    }

    @SubscribeEvent
    public static void playerDowned(PlayerDownedEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            DeathHintEvents.CONTEXT.downed(player.getUUID(), DeathHintEvents.classify(event.getSource()));
            String episode = player.getUUID() + ":downed:" + player.server.getTickCount();
            ThreadSignals.emit(player, "downed", "player", episode);
        }
    }
}
