package com.bettercontent.threads;
import com.bettercontent.threads.compat.ValkyrienSkiesThreads;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;

/** Correlates a passenger with actual motion of the same vessel. */
public final class PackActionThreads {
    private static final String VESSEL = "BetterContentThreadsVessel";
    private PackActionThreads() {}
    @SubscribeEvent public static void serverTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.getServer().getTickCount() % 20 != 0 || !ModList.get().isLoaded("valkyrienskies")) return;
        for (var player : event.getServer().getPlayerList().getPlayers()) updateVessel(player);
    }
    private static void updateVessel(ServerPlayer player) {
        var root = player.getPersistentData();
        CompoundTag state = root.getCompound(VESSEL);
        VesselObservation vessel = ValkyrienSkiesThreads.observe(player);
        if (vessel == null) { root.remove(VESSEL); return; }
        if (!vessel.id().equals(state.getString("ship"))) {
            state = new CompoundTag(); state.putString("ship", vessel.id());
            state.putString("token", java.util.UUID.randomUUID().toString());
            state.putDouble("startX", vessel.x()); state.putDouble("startZ", vessel.z());
        } else if (!state.getBoolean("discovered")) {
            double dx = vessel.x() - state.getDouble("startX"), dz = vessel.z() - state.getDouble("startZ");
            if (dx * dx + dz * dz >= 16.0) {
                ThreadSignals.emit(player, "vessel_moved", "carried", state.getString("token"));
                state.putBoolean("discovered", true);
            }
        }
        root.put(VESSEL, state);
    }
    public record VesselObservation(String id, double x, double z) {}
}
