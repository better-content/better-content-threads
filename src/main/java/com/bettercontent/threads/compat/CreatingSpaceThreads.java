package com.bettercontent.threads.compat;
import com.bettercontent.threads.ThreadSignals;
import com.rae.creatingspace.content.rocket.RocketContraptionEntity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.EntityMountEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
/** Actual mounted journey correlation; readiness estimates never award a discovery. */
public final class CreatingSpaceThreads {
    private static final String JOURNEY = "BetterContentThreadsRocketJourney";
    private CreatingSpaceThreads() {}
    @SubscribeEvent public static void mounted(EntityMountEvent event) {
        if (!(event.getEntityMounting() instanceof ServerPlayer player)
            || !(event.getEntityBeingMounted() instanceof RocketContraptionEntity rocket)) return;
        if (event.isMounting()) player.getPersistentData().putString(JOURNEY, rocket.getUUID().toString());
        // Dimension transfer unmounts before PlayerChangedDimensionEvent. Keep the token only for this tick.
        else player.getPersistentData().putLong(JOURNEY + "Unmount", player.server.getTickCount());
    }
    public static void arrived(ServerPlayer player, String destination) {
        var state = player.getPersistentData();
        String token = state.getString(JOURNEY);
        boolean aboard = player.getVehicle() instanceof RocketContraptionEntity;
        boolean transfer = state.contains(JOURNEY + "Unmount") && state.getLong(JOURNEY + "Unmount") == player.server.getTickCount();
        if (!token.isBlank() && (aboard || transfer) && destination.equals("creatingspace:earth_orbit"))
            ThreadSignals.emit(player, "orbit_reached", destination, token);
        state.remove(JOURNEY); state.remove(JOURNEY + "Unmount");
    }
}
