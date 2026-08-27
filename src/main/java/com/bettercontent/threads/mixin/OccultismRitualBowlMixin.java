package com.bettercontent.threads.mixin;

import com.bettercontent.threads.PackActionThreads;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(targets = "com.klikli_dev.occultism.common.blockentity.GoldenSacrificialBowlBlockEntity", remap = false)
public abstract class OccultismRitualBowlMixin {
    @Shadow(remap = false) public ServerPlayer castingPlayer;

    @Inject(method = "startRitual", at = @At("TAIL"), remap = false)
    private void threads$ritualStarted(ServerPlayer player, ItemStack activationItem, @Coerce Object recipe, CallbackInfo callback) {
        PackActionThreads.occultismStarted(player);
    }

    @Inject(method = "stopRitual", at = @At("HEAD"), remap = false)
    private void threads$ritualStopped(boolean finished, CallbackInfo callback) {
        if (finished) PackActionThreads.occultismFinished(castingPlayer);
    }
}
