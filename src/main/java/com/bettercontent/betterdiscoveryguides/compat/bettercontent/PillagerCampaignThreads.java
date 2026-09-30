package com.bettercontent.betterdiscoveryguides.compat.bettercontent;

import com.bettercontent.betterpillagercampaigns.api.CampaignStatusApi;
import net.minecraft.server.level.ServerPlayer;

import java.util.Locale;

/** Typed Pillager Campaigns access isolated behind the loaded-mod guard in ThreadEvents. */
public final class PillagerCampaignThreads {
    private PillagerCampaignThreads() {}

    @net.minecraftforge.eventbus.api.SubscribeEvent
    public static void materialized(com.bettercontent.betterpillagercampaigns.api.CampaignMaterializedEvent event) {
        if (event.getMembers() > 0) com.bettercontent.betterdiscoveryguides.ThreadSignals.emit(event.getPlayer(), "campaign_attack", "materialized", event.getInvasionId());
    }

    public static String state(ServerPlayer player) {
        return CampaignStatusApi.state(player).name().toLowerCase(Locale.ROOT);
    }
}
