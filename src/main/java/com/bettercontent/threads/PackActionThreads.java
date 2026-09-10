package com.bettercontent.threads;

import com.bettercontent.threads.compat.ValkyrienSkiesThreads;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;


/**
 * Action-backed bridges for optional pack mods that do not publish a stable
 * Forge event for the operation Threads needs to observe.
 */
public final class PackActionThreads {
    private static final String VESSEL = "BetterContentThreadsVessel";

    private PackActionThreads() {}

    @SubscribeEvent
    public static void serverTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.getServer().getTickCount() % 20 != 0) return;
        for (ServerPlayer player : event.getServer().getPlayerList().getPlayers()) updateVessel(player);
    }

    /** Called after AE2's CPU declares an actual autocrafting job finished. */
    public static void ae2CraftFinished(ServerLevel level, BlockPos position) {
        ServerPlayer player = nearest(level, position, 128.0);
        if (player == null) return;
        completeActive(player, "machines_can_remember", "machine_recall", "ae2");
        completeActive(player, "source_becomes_machinery", "source_machine", "complete");
    }

    /** Called only when Ars Energistique converts a positive Source amount into AE power. */
    public static void sourceConverted(Object sink, int amount) {
        if (amount <= 0) return;
        ServerPlayer player = nearestBlockEntityPlayer(sink, 24.0);
        if (player == null) return;
        ThreadSignals.emit(player, "source_machine", "connected", episode(player, "arseng-source"));
    }

    public static void occultismStarted(ServerPlayer player) {
        if (player != null) ThreadSignals.emit(player, "spirit_binding", "occultism", episode(player, "occultism-ritual"));
    }

    public static void occultismFinished(ServerPlayer player) {
        if (player != null) completeActive(player, "spirits_honour_contracts", "spirit_complete", "occultism");
    }

    private static void updateVessel(ServerPlayer player) {
        CompoundTag state = persisted(player).getCompound(VESSEL);
        VesselObservation vessel = observeVessel(player);
        if (vessel == null) {
            if (state.getBoolean("aboard")) {
                String token = state.getString("token");
                if (state.getDouble("distance") >= 128.0 && token.equals(ThreadSignals.activeCorrelation(player, "vessel_becomes_place"))) {
                    ThreadSignals.emit(player, "vessel_landfall", "128", token);
                }
                persisted(player).remove(VESSEL);
            }
            return;
        }

        String shipId = vessel.id();
        if (!state.getBoolean("aboard") || !shipId.equals(state.getString("ship"))) {
            String token = episode(player, "vessel:" + shipId);
            state = new CompoundTag();
            state.putBoolean("aboard", true);
            state.putString("ship", shipId);
            state.putString("token", token);
            state.putDouble("startX", vessel.x());
            state.putDouble("startZ", vessel.z());
            ThreadSignals.emit(player, "vessel_assemble", shipId, token);
        } else {
            double dx = vessel.x() - state.getDouble("startX");
            double dz = vessel.z() - state.getDouble("startZ");
            state.putDouble("distance", Math.max(state.getDouble("distance"), Math.sqrt(dx * dx + dz * dz)));
        }
        persisted(player).put(VESSEL, state);
    }

    private static VesselObservation observeVessel(ServerPlayer player) {
        if (!ModList.get().isLoaded("valkyrienskies")) return null;
        return ValkyrienSkiesThreads.observe(player);
    }

    private static ServerPlayer nearestBlockEntityPlayer(Object target, double radius) {
        if (!(target instanceof BlockEntity blockEntity) || !(blockEntity.getLevel() instanceof ServerLevel level)) return null;
        return nearest(level, blockEntity.getBlockPos(), radius);
    }

    private static ServerPlayer nearest(ServerLevel level, BlockPos pos, double radius) {
        ServerPlayer best = null;
        double bestDistance = radius * radius;
        for (ServerPlayer player : level.players()) {
            double distance = player.distanceToSqr(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
            if (distance <= bestDistance) {
                best = player;
                bestDistance = distance;
            }
        }
        return best;
    }

    private static void completeActive(ServerPlayer player, String card, String type, String value) {
        String token = ThreadSignals.activeCorrelation(player, card);
        if (token != null) ThreadSignals.emit(player, type, value, token);
    }

    private static CompoundTag persisted(ServerPlayer player) {
        CompoundTag root = player.getPersistentData();
        if (!root.contains(net.minecraft.world.entity.player.Player.PERSISTED_NBT_TAG)) {
            root.put(net.minecraft.world.entity.player.Player.PERSISTED_NBT_TAG, new CompoundTag());
        }
        return root.getCompound(net.minecraft.world.entity.player.Player.PERSISTED_NBT_TAG);
    }

    private static String episode(ServerPlayer player, String kind) {
        return player.getUUID() + ":" + Integer.toUnsignedString(kind.hashCode(), 36) + ":" + player.server.getTickCount();
    }

    public record VesselObservation(String id, double x, double z) {}
}
