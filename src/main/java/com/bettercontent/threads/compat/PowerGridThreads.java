package com.bettercontent.threads.compat;
import com.bettercontent.threads.OperationOwners;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import org.patryk3211.powergrid.electricity.base.ElectricBehaviour;
import org.patryk3211.powergrid.kinetics.motor.ElectricMotorBlockEntity;
import org.patryk3211.powergrid.kinetics.motor.ConstantSpeedMotorBlockEntity;
/** Called only after a connected native consumer has produced its finished output. */
public final class PowerGridThreads {
    private PowerGridThreads() {}
    public static void completed(KineticBlockEntity consumer) {
        if (consumer.getLevel() == null || consumer.getLevel().isClientSide
            || !consumer.hasNetwork() || consumer.getSpeed() == 0) return;
        for (var motor : consumer.getOrCreateNetwork().sources.keySet()) {
            if (!(motor instanceof ElectricMotorBlockEntity || motor instanceof ConstantSpeedMotorBlockEntity)
                || motor.getGeneratedSpeed() == 0) continue;
            var electricity = motor.getBehaviour(ElectricBehaviour.TYPE);
            if (electricity == null || electricity.getConnections().values().stream().allMatch(java.util.Set::isEmpty)) continue;
            boolean powered = electricity.getExternalNodes().stream()
                .anyMatch(n -> Math.abs(n.getCurrent()) > 0.0001 && Math.abs(n.getVoltage()) > 0.01);
            if (powered) OperationOwners.completed(motor, "electric_complete", "generated_transmitted_consumed",
                "Connected electricity powered a completed press operation");
        }
    }
}
