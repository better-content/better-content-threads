package com.bettercontent.threads.compat;

import com.bettercontent.threads.ThreadSignals;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.patryk3211.powergrid.electricity.base.ElectricBehaviour;
import org.patryk3211.powergrid.electricity.sim.node.IElectricNode;

/** Loaded only with Power Grid; observes its typed electrical graph at block interaction time. */
public final class PowerGridThreads {
    private PowerGridThreads() {}

    @SubscribeEvent
    public static void interacted(PlayerInteractEvent.RightClickBlock event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        BlockEntity blockEntity = player.level().getBlockEntity(event.getPos());
        if (!(blockEntity instanceof SmartBlockEntity smartBlockEntity)) return;
        ElectricalObservation observation = observeElectric(smartBlockEntity);
        if (!observation.working()) return;
        String active = ThreadSignals.activeCorrelation(player, "electricity_agreement");
        if (active == null) {
            ThreadSignals.emit(player, "electric_network", "connected", episode(player));
        } else if (observation.consumer()) {
            ThreadSignals.emit(player, "electric_complete", "generated_transmitted_consumed", active);
        }
    }

    private static ElectricalObservation observeElectric(SmartBlockEntity blockEntity) {
        ElectricBehaviour behavior = blockEntity.getBehaviour(ElectricBehaviour.TYPE);
        if (behavior == null) return ElectricalObservation.NONE;
        boolean transmitted = behavior.getConnections().values().stream().anyMatch(connections -> !connections.isEmpty());
        boolean energized = false;
        for (IElectricNode node : behavior.getExternalNodes()) {
            if (Math.abs(node.getCurrent()) > 0.0001 && Math.abs(node.getVoltage()) > 0.01) {
                energized = true;
                break;
            }
        }
        String name = blockEntity.getClass().getName();
        boolean consumer = name.contains(".motor.") || name.contains(".fan.") || name.contains(".heater.")
            || name.contains(".basinheater.") || name.contains(".light.") || name.contains(".plotter.");
        return new ElectricalObservation(transmitted && energized, consumer);
    }

    private static String episode(ServerPlayer player) {
        return player.getUUID() + ":powergrid:" + player.server.getTickCount();
    }

    private record ElectricalObservation(boolean working, boolean consumer) {
        private static final ElectricalObservation NONE = new ElectricalObservation(false, false);
    }
}
