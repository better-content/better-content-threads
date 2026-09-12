package com.bettercontent.threads.compat.bettercontent;
import com.bettercontent.economy.api.event.SpiritReleasedEvent;
import com.bettercontent.threads.ThreadSignals;
import net.minecraftforge.eventbus.api.SubscribeEvent;
public final class EconomyThreads {
    private EconomyThreads() {}
    @SubscribeEvent public static void spiritAcquired(SpiritReleasedEvent event) {
        if (com.bettercontent.economy.spirit.SpiritKind.fromId(event.getSpiritId().toString()) == null) return;
        ThreadSignals.emit(event.getPlayer(), "spirit_acquired", event.getSpiritId().toString(), event.getSpiritEntityUuid().toString());
    }
}
