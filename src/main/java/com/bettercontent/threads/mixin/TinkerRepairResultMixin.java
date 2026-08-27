package com.bettercontent.threads.mixin;

import com.bettercontent.threads.compat.TConstructThreads;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(targets = "slimeknights.tconstruct.tables.block.entity.inventory.LazyResultContainer", remap = false)
abstract class TinkerRepairResultMixin {
    @Inject(method = "craftResult(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/item/ItemStack;I)V", at = @At("HEAD"), remap = false)
    private void betterContentThreads$repair(Player player, ItemStack output, int amount, CallbackInfo callback) {
        if (player instanceof ServerPlayer serverPlayer) TConstructThreads.repaired(serverPlayer, output);
    }
}
