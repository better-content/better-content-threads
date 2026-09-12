package com.bettercontent.threads.mixin;

import appeng.api.networking.IGrid;
import appeng.api.networking.crafting.*;
import appeng.api.networking.security.IActionSource;
import appeng.me.cluster.implementations.CraftingCPUCluster;
import com.bettercontent.threads.ThreadSignals;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;
import java.util.UUID;

/** The native success boolean distinguishes completion from cancellation; actor follows the job through NBT. */
@Pseudo
@Mixin(targets = "appeng.crafting.execution.CraftingCpuLogic", remap = false)
public abstract class Ae2CraftingCpuMixin {
    @Shadow @Final private CraftingCPUCluster cluster;
    @Unique private UUID threads$owner;
    @Unique private String threads$job = "";
    @Unique private String threads$result = "";
    @Inject(method = "trySubmitJob", at = @At("RETURN"), remap = false)
    private void threads$submitted(IGrid grid, ICraftingPlan plan, IActionSource source, ICraftingRequester requester,
                                  CallbackInfoReturnable<ICraftingSubmitResult> cir) {
        if (!cir.getReturnValue().successful()) return;
        threads$owner = source.player().map(p -> p.getUUID()).orElse(null);
        threads$job = UUID.randomUUID().toString();
        threads$result = plan.finalOutput().what().getDisplayName().getString();
    }
    @Inject(method = "finishJob", at = @At("HEAD"), remap = false)
    private void threads$finished(boolean success, CallbackInfo ci) {
        if (success && threads$owner != null && cluster.getLevel() instanceof ServerLevel level) {
            ThreadSignals.emit(level.getServer(), threads$owner, "machine_recall", "ae2", threads$job,
                "AE2: " + threads$result + " at " + cluster.getBoundsMin().toShortString());
            var device = level.getBlockEntity(cluster.getBoundsMin());
            if (device != null && net.minecraftforge.fml.ModList.get().isLoaded("arcane_chunk_loaders"))
                com.bettercontent.threads.compat.bettercontent.ArcaneChunkLoaderThreads.workCompleted(device, threads$owner, threads$job, "AE2: " + threads$result);
        }
        threads$owner = null; threads$job = ""; threads$result = "";
    }
    @Inject(method = "writeToNBT", at = @At("TAIL"), remap = false)
    private void threads$save(CompoundTag tag, CallbackInfo ci) {
        if (threads$owner == null) { tag.remove("ThreadsJob"); return; }
        CompoundTag job = new CompoundTag();
        job.putUUID("owner", threads$owner); job.putString("operation", threads$job); job.putString("result", threads$result);
        tag.put("ThreadsJob", job);
    }
    @Inject(method = "readFromNBT", at = @At("TAIL"), remap = false)
    private void threads$load(CompoundTag tag, CallbackInfo ci) {
        var job = tag.getCompound("ThreadsJob");
        threads$owner = job.hasUUID("owner") ? job.getUUID("owner") : null;
        threads$job = job.getString("operation"); threads$result = job.getString("result");
    }
}
