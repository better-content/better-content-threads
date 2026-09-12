package com.bettercontent.threads.mixin;
import com.bettercontent.threads.OperationOwners;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Pseudo
@Mixin(targets = "com.klikli_dev.occultism.common.blockentity.GoldenSacrificialBowlBlockEntity", remap = false)
public abstract class OccultismRitualBowlMixin {
    @Inject(method = "startRitual", at = @At("TAIL"), remap = false)
    private void threads$started(ServerPlayer player, ItemStack item, @Coerce Object recipe, CallbackInfo ci) {
        if (player != null) OperationOwners.configure((BlockEntity)(Object)this, player.getUUID());
    }
    @Inject(method = "stopRitual", at = @At("HEAD"), remap = false)
    private void threads$finished(boolean finished, CallbackInfo ci) {
        if (finished) OperationOwners.completed((BlockEntity)(Object)this, "spirit_complete", "occultism", "The ritual completed successfully");
    }
}
