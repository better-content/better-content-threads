package com.bettercontent.threads.mixin;
import com.bettercontent.threads.compat.BloodMagicThreads;
import wayoftime.bloodmagic.common.tile.TileAltar;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Pseudo
@Mixin(targets = "wayoftime.bloodmagic.altar.BloodAltar", remap = false)
public abstract class BloodAltarRecipeMixin {
    @Shadow private TileAltar tileAltar;
    @Unique private ItemStack threads$input;
    @Inject(method = "updateAltar", at = @At("HEAD"), remap = false)
    private void threads$before(CallbackInfo ci) { threads$input = tileAltar.getItem(0).copy(); }
    @Inject(method = "updateAltar", at = @At("RETURN"), remap = false)
    private void threads$crafted(CallbackInfo ci) {
        if (!threads$input.isEmpty() && !ItemStack.matches(threads$input, tileAltar.getItem(0))) BloodMagicThreads.crafted(tileAltar);
    }
}
