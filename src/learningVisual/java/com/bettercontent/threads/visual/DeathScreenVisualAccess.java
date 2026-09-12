package com.bettercontent.threads.visual;
import net.minecraft.client.gui.screens.DeathScreen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
/** Official-mapped visual fixture only; absent from the deployable mod. */
@Mixin(value=DeathScreen.class,remap=false)
public interface DeathScreenVisualAccess {
    @Accessor(value="deathScore",remap=false) void threads$score(Component score);
}
