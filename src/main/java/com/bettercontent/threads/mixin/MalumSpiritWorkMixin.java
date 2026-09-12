package com.bettercontent.threads.mixin;
import com.bettercontent.threads.OperationOwners;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Pseudo
@Mixin(targets = "com.sammy.malum.common.block.curiosities.spirit_altar.SpiritAltarBlockEntity", remap = false)
public abstract class MalumSpiritWorkMixin {
    @Inject(method = "craft", at = @At("TAIL"), remap = false)
    private void threads$crafted(CallbackInfo ci) {
        OperationOwners.completed((BlockEntity)(Object)this, "spirit_ritual", "malum", "Spirits completed an infusion at the spirit altar");
    }
}
