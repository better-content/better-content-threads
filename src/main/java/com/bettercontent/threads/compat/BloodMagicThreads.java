package com.bettercontent.threads.compat;
import com.bettercontent.threads.OperationOwners;
import net.minecraft.world.level.block.entity.BlockEntity;
/** The altar recipe result has already been written when this is called. */
public final class BloodMagicThreads {
    private BloodMagicThreads() {}
    public static void crafted(BlockEntity altar) {
        OperationOwners.completed(altar, "blood_complete", "altar_recipe", "Life Essence completed an altar recipe");
    }
}
