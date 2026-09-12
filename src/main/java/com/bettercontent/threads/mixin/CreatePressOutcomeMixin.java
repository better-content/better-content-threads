package com.bettercontent.threads.mixin;
import com.bettercontent.threads.compat.CreateOutcomeThreads;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Pseudo
@Mixin(targets = "com.simibubi.create.content.kinetics.press.MechanicalPressBlockEntity", remap = false)
public abstract class CreatePressOutcomeMixin {
    @Inject(method = "onItemPressed", at = @At("TAIL"), remap = false)
    private void threads$pressed(ItemStack output, CallbackInfo ci) {
        if (!output.isEmpty()) CreateOutcomeThreads.pressed((KineticBlockEntity)(Object)this);
    }
}
