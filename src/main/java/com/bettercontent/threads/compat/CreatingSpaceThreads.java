package com.bettercontent.threads.compat;

import com.bettercontent.threads.ThreadSignals;
import com.rae.creatingspace.content.rocket.RocketContraptionEntity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.EntityMountEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/** Version-pinned Creating Space 1.7.13 assembled-rocket evidence. */
public final class CreatingSpaceThreads {
    private CreatingSpaceThreads() {}

    @SubscribeEvent
    public static void mountReadyRocket(EntityMountEvent event) {
        if (!event.isMounting() || !(event.getEntityMounting() instanceof ServerPlayer player)) return;
        if (!(event.getEntityBeingMounted() instanceof RocketContraptionEntity rocket)) return;
        if (rocket.destination == null || rocket.totalThrust <= 0.0f || rocket.deltaV() <= 0.0f) return;
        String token = player.getUUID() + ":rocket:" + rocket.getUUID();
        ThreadSignals.emit(player, "rocket_ready", rocket.destination.toString(), token);
    }
}
