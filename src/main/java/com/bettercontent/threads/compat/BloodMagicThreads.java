package com.bettercontent.threads.compat;

import com.bettercontent.threads.ThreadSignals;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import wayoftime.bloodmagic.event.SacrificeKnifeUsedEvent;
import wayoftime.bloodmagic.event.SoulNetworkEvent;

/** Player-owned Blood Magic life-power episode using the mod's native events. */
public final class BloodMagicThreads {
    private BloodMagicThreads() {}

    @SubscribeEvent
    public static void altarFed(SacrificeKnifeUsedEvent event) {
        if (!(event.player instanceof ServerPlayer player) || !event.shouldFillAltar || event.lpAdded <= 0) return;
        String token = player.getUUID() + ":blood:" + player.server.getTickCount();
        ThreadSignals.emit(player, "blood_altar", "bound", token);
    }

    @SubscribeEvent
    public static void lifePowerSpent(SoulNetworkEvent.Syphon.User event) {
        if (!(event.getUser() instanceof ServerPlayer player)) return;
        String token = ThreadSignals.activeCorrelation(player, "blood_infrastructure");
        if (token != null) ThreadSignals.emit(player, "blood_complete", "player_lp", token);
    }
}
