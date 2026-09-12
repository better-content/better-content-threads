package com.bettercontent.threads.compat;
import com.bettercontent.threads.ThreadSignals;
import com.ferreusveritas.dynamictrees.block.branch.BranchBlock;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.state.BlockState;
/** Called only after native felling has spawned its falling tree. */
public final class DynamicTreeThreads {
    private DynamicTreeThreads() {}
    public static void felled(ServerPlayer player, BlockState state) {
        if (!(state.getBlock() instanceof BranchBlock branch) || branch.getRadius(state) < 8) return;
        ThreadSignals.emit(player, "tree_felled", "natural_mature", java.util.UUID.randomUUID().toString());
    }
}
