package com.bettercontent.threads;

import com.bettercontent.threads.compat.ValkyrienSkiesThreads;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerContainerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Map;

/**
 * Action-backed bridges for optional pack mods that do not publish a stable
 * Forge event for the operation Threads needs to observe.
 */
public final class PackActionThreads {
    private static final String VESSEL = "BetterContentThreadsVessel";

    private PackActionThreads() {}

    @SubscribeEvent
    public static void openedContainer(PlayerContainerEvent.Open event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        String menu = event.getContainer().getClass().getName();
        if (!menu.contains("PatternEncoding")) return;
        String token = episode(player, "ae2-pattern");
        ThreadSignals.emit(player, "machine_memory", "ae2", token);
    }

    @SubscribeEvent
    public static void interacted(PlayerInteractEvent.RightClickBlock event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        BlockEntity blockEntity = player.level().getBlockEntity(event.getPos());
        if (blockEntity == null || !blockEntity.getClass().getName().startsWith("org.patryk3211.powergrid.")) return;
        ElectricalObservation observation = observeElectric(blockEntity);
        if (!observation.working()) return;
        String active = ThreadSignals.activeCorrelation(player, "electricity_agreement");
        if (active == null) {
            ThreadSignals.emit(player, "electric_network", "connected", episode(player, "powergrid"));
        } else if (observation.consumer()) {
            ThreadSignals.emit(player, "electric_complete", "generated_transmitted_consumed", active);
        }
    }

    @SubscribeEvent
    public static void serverTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.getServer().getTickCount() % 20 != 0) return;
        for (ServerPlayer player : event.getServer().getPlayerList().getPlayers()) updateVessel(player);
    }

    /** Called after AE2's CPU declares an actual autocrafting job finished. */
    public static void ae2CraftFinished(Object cpu) {
        try {
            Object levelObject = cpu.getClass().getMethod("getLevel").invoke(cpu);
            Object posObject = cpu.getClass().getMethod("getBoundsMin").invoke(cpu);
            if (!(levelObject instanceof ServerLevel level) || !(posObject instanceof BlockPos pos)) return;
            ServerPlayer player = nearest(level, pos, 128.0);
            if (player == null) return;
            completeActive(player, "machines_can_remember", "machine_recall", "ae2");
            completeActive(player, "source_becomes_machinery", "source_machine", "complete");
        } catch (ReflectiveOperationException ignored) {
            // Optional integration: a changed foreign API must not destabilize the pack.
        }
    }

    /** Called only when Ars Energistique converts a positive Source amount into AE power. */
    public static void sourceConverted(Object adaptor, int amount) {
        if (amount <= 0) return;
        try {
            Object sink = adaptor.getClass().getMethod("sink").invoke(adaptor);
            ServerPlayer player = nearestBlockEntityPlayer(sink, 24.0);
            if (player == null) return;
            ThreadSignals.emit(player, "source_machine", "connected", episode(player, "arseng-source"));
        } catch (ReflectiveOperationException ignored) {
            // Ars Energistique is optional.
        }
    }

    public static void occultismStarted(ServerPlayer player) {
        if (player != null) ThreadSignals.emit(player, "spirit_binding", "occultism", episode(player, "occultism-ritual"));
    }

    public static void occultismFinished(ServerPlayer player) {
        if (player != null) completeActive(player, "spirits_honour_contracts", "spirit_complete", "occultism");
    }

    private static ElectricalObservation observeElectric(BlockEntity blockEntity) {
        try {
            Method getter = blockEntity.getClass().getMethod("getElectricBehaviour");
            Object behavior = getter.invoke(blockEntity);
            if (behavior == null) return ElectricalObservation.NONE;
            Object nodesObject = behavior.getClass().getMethod("getExternalNodes").invoke(behavior);
            Object connectionsObject = behavior.getClass().getMethod("getConnections").invoke(behavior);
            if (!(nodesObject instanceof Collection<?> nodes) || !(connectionsObject instanceof Map<?, ?> connections)) return ElectricalObservation.NONE;
            boolean transmitted = connections.values().stream().anyMatch(value -> value instanceof Collection<?> collection && !collection.isEmpty());
            boolean energized = false;
            for (Object node : nodes) {
                double current = number(node, "getCurrent");
                double voltage = number(node, "getVoltage");
                if (Math.abs(current) > 0.0001 && Math.abs(voltage) > 0.01) {
                    energized = true;
                    break;
                }
            }
            String name = blockEntity.getClass().getName();
            boolean consumer = name.contains(".motor.") || name.contains(".fan.") || name.contains(".heater.")
                || name.contains(".basinheater.") || name.contains(".light.") || name.contains(".plotter.");
            return new ElectricalObservation(transmitted && energized, consumer);
        } catch (ReflectiveOperationException ignored) {
            return ElectricalObservation.NONE;
        }
    }

    private static double number(Object target, String method) throws ReflectiveOperationException {
        return ((Number) target.getClass().getMethod(method).invoke(target)).doubleValue();
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
        try {
            return ValkyrienSkiesThreads.observe(player);
        } catch (LinkageError ignored) {
            // The optional integration remains inert if a foreign binary API changes.
            return null;
        }
    }

    private static ServerPlayer nearestBlockEntityPlayer(Object target, double radius) throws ReflectiveOperationException {
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

    private record ElectricalObservation(boolean working, boolean consumer) {
        private static final ElectricalObservation NONE = new ElectricalObservation(false, false);
    }

    public record VesselObservation(String id, double x, double z) {}
}
