package com.bettercontent.betterdiscoveryguides.compat.bettercontent;
import com.bettercontent.bettercreatekineticloss.api.TransmissionOverloadEvent;
import com.bettercontent.betterdiscoveryguides.OperationOwners;
import com.bettercontent.betterdiscoveryguides.ThreadSignals;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import java.util.*;
public final class TransmissionLossThreads {
    private TransmissionLossThreads() {}
    @SubscribeEvent public static void overloaded(TransmissionOverloadEvent event) {
        Set<UUID> credited = new HashSet<>();
        for (var source : event.getNetwork().sources.keySet()) {
            UUID owner = OperationOwners.owner(source);
            if (owner == null || !credited.add(owner) || !(source.getLevel() instanceof ServerLevel level)) continue;
            ThreadSignals.emit(level.getServer(), owner, "transmission", "overloaded", UUID.randomUUID().toString(),
                "Load " + event.getBaseStress() + " + transmission " + event.getLoss() + " exceeds capacity " + event.getCapacity());
        }
    }
}
