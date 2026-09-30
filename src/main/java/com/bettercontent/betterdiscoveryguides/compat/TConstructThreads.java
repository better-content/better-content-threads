package com.bettercontent.betterdiscoveryguides.compat;
import com.bettercontent.betterdiscoveryguides.OperationOwners;
import net.minecraft.world.level.block.entity.BlockEntity;
/** Ownership comes from constructing the controller, never inspecting alloyable tanks. */
public final class TConstructThreads {
    private TConstructThreads() {}
    public static void alloyed(BlockEntity smeltery) {
        OperationOwners.completed(smeltery, "alloy_cast", "tconstruct", "Component fluids became an alloy");
    }
}
