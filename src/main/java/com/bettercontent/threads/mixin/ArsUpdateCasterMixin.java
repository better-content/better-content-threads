package com.bettercontent.threads.mixin;

import com.bettercontent.threads.compat.ArsNouveauThreads;
import com.hollingsworth.arsnouveau.api.spell.Spell;
import net.minecraftforge.network.NetworkEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Supplier;

@Pseudo
@Mixin(targets = "com.hollingsworth.arsnouveau.common.network.PacketUpdateCaster", remap = false)
abstract class ArsUpdateCasterMixin {
    @Shadow(remap = false) Spell spellRecipe;

    @Inject(method = "handle", at = @At("HEAD"), remap = false)
    private void betterContentThreads$authored(Supplier<NetworkEvent.Context> supplier, CallbackInfo callback) {
        NetworkEvent.Context context = supplier.get();
        if (context.getSender() != null) {
            Spell snapshot = spellRecipe.clone();
            context.enqueueWork(() -> ArsNouveauThreads.authored(context.getSender(), snapshot));
        }
    }
}
