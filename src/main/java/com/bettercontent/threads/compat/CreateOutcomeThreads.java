package com.bettercontent.threads.compat;
import com.bettercontent.threads.OperationOwners;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.content.kinetics.crank.HandCrankBlockEntity;
/** Checks the native power provenance of a completed press operation. */
public final class CreateOutcomeThreads {
    private CreateOutcomeThreads() {}
    public static void pressed(KineticBlockEntity press) {
        if (!press.hasNetwork() || press.getSpeed() == 0) return;
        if (net.minecraftforge.fml.ModList.get().isLoaded("powergrid")) PowerGridThreads.completed(press);
        var sources = press.getOrCreateNetwork().sources.keySet().stream().filter(s -> s.getGeneratedSpeed() != 0).toList();
        if (sources.isEmpty() || sources.stream().anyMatch(s -> !(s instanceof HandCrankBlockEntity))) return;
        for (var crank : sources) OperationOwners.completed(crank, "create_manual_complete", "hand_crank", "Your hand crank powered a completed press operation");
    }
}
