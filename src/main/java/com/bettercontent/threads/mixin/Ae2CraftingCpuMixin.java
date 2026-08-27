package com.bettercontent.threads.mixin;

import com.bettercontent.threads.PackActionThreads;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(targets = "appeng.me.cluster.implementations.CraftingCPUCluster", remap = false)
public abstract class Ae2CraftingCpuMixin {
    @Inject(method = "done", at = @At("TAIL"), remap = false)
    private void threads$jobFinished(CallbackInfo callback) {
        PackActionThreads.ae2CraftFinished(this);
    }
}
