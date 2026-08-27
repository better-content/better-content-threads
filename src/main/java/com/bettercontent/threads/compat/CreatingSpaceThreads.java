package com.bettercontent.threads.compat;

import com.bettercontent.threads.ThreadSignals;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.EntityMountEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/** Version-pinned Creating Space 1.7.13 assembled-rocket evidence. */
public final class CreatingSpaceThreads {
    private static final String ROCKET = "com.rae.creatingspace.content.rocket.RocketContraptionEntity";

    private CreatingSpaceThreads() {}

    @SubscribeEvent
    public static void mountReadyRocket(EntityMountEvent event) {
        if (!event.isMounting() || !(event.getEntityMounting() instanceof ServerPlayer player)) return;
        Object rocket = event.getEntityBeingMounted();
        if (!rocket.getClass().getName().equals(ROCKET)) return;
        try {
            ResourceLocation destination = (ResourceLocation) rocket.getClass().getField("destination").get(rocket);
            float thrust = rocket.getClass().getField("totalThrust").getFloat(rocket);
            float deltaV = ((Number) rocket.getClass().getMethod("deltaV").invoke(rocket)).floatValue();
            if (destination == null || thrust <= 0.0f || deltaV <= 0.0f) return;
            String token = player.getUUID() + ":rocket:" + event.getEntityBeingMounted().getUUID();
            ThreadSignals.emit(player, "rocket_ready", destination.toString(), token);
        } catch (ReflectiveOperationException failure) {
            throw new IllegalStateException("Creating Space 1.7.13 rocket API changed", failure);
        }
    }
}
