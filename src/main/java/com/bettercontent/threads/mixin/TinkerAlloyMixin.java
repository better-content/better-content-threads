package com.bettercontent.threads.mixin;
import com.bettercontent.threads.compat.TConstructThreads;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import slimeknights.mantle.block.entity.MantleBlockEntity;
import slimeknights.tconstruct.library.recipe.alloying.IAlloyTank;
@Pseudo
@Mixin(targets = "slimeknights.tconstruct.smeltery.block.entity.module.alloying.MultiAlloyingModule", remap = false)
public abstract class TinkerAlloyMixin {
    @Shadow private MantleBlockEntity parent;
    @Shadow @Final private IAlloyTank alloyTank;
    @Unique private String threads$before;
    @Unique private String threads$fluids() {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < alloyTank.getTanks(); i++) result.append(alloyTank.getFluidInTank(i).writeToNBT(new net.minecraft.nbt.CompoundTag()));
        return result.toString();
    }
    @Inject(method = "doAlloy", at = @At("HEAD"), remap = false)
    private void threads$before(CallbackInfo ci) { threads$before = threads$fluids(); }
    @Inject(method = "doAlloy", at = @At("RETURN"), remap = false)
    private void threads$alloyed(CallbackInfo ci) {
        if (!threads$before.equals(threads$fluids())) TConstructThreads.alloyed(parent);
    }
}
