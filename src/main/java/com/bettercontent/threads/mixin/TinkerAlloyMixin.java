package com.bettercontent.threads.mixin;

import com.bettercontent.threads.compat.TConstructThreads;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import slimeknights.mantle.block.entity.MantleBlockEntity;

@Pseudo
@Mixin(targets = "slimeknights.tconstruct.smeltery.block.entity.module.alloying.MultiAlloyingModule", remap = false)
abstract class TinkerAlloyMixin {
    @Shadow(remap = false) private MantleBlockEntity parent;

    @Inject(method = "doAlloy", at = @At("TAIL"), remap = false)
    private void betterContentThreads$alloyed(CallbackInfo callback) {
        TConstructThreads.alloyed(parent);
    }
}
