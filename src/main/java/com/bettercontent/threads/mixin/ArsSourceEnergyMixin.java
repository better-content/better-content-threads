package com.bettercontent.threads.mixin;

import com.bettercontent.threads.PackActionThreads;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(targets = "gripe._90.arseng.me.misc.SourceEnergyAdaptor", remap = false)
public abstract class ArsSourceEnergyMixin {
    @Inject(method = "addSource", at = @At("RETURN"), remap = false)
    private void threads$sourceConverted(int amount, CallbackInfoReturnable<Integer> callback) {
        PackActionThreads.sourceConverted(this, amount);
    }
}
