package com.bettercontent.threads.compat;

import appeng.menu.me.items.PatternEncodingTermMenu;
import com.bettercontent.threads.ThreadSignals;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.player.PlayerContainerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/** Loaded only with AE2; observes its typed pattern terminal menu. */
public final class Ae2Threads {
    private Ae2Threads() {}

    @SubscribeEvent
    public static void openedContainer(PlayerContainerEvent.Open event) {
        if (!(event.getEntity() instanceof ServerPlayer player)
            || !(event.getContainer() instanceof PatternEncodingTermMenu)) return;
        String token = player.getUUID() + ":ae2-pattern:" + player.server.getTickCount();
        ThreadSignals.emit(player, "machine_memory", "ae2", token);
    }
}
