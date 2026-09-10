package com.bettercontent.threads.compat.bettercontent;

import com.bettercontent.pillagercampaigns.api.CampaignStatusApi;
import net.minecraft.server.level.ServerPlayer;

import java.util.Locale;

/** Typed Pillager Campaigns access isolated behind the loaded-mod guard in ThreadEvents. */
public final class PillagerCampaignThreads {
    private PillagerCampaignThreads() {}

    public static String state(ServerPlayer player) {
        return CampaignStatusApi.state(player).name().toLowerCase(Locale.ROOT);
    }
}
