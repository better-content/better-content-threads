package com.bettercontent.learningsurfaces.mixin;

import com.bettercontent.learningsurfaces.LoadingLessonClient;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.ReceivingLevelScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ReceivingLevelScreen.class)
public abstract class ReceivingLevelScreenLoadingMixin extends Screen {
    protected ReceivingLevelScreenLoadingMixin(Component title) {
        super(title);
    }

    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void learningSurfaces$render(GuiGraphics graphics, int mouseX, int mouseY,
                                             float partialTick, CallbackInfo callback) {
        if (!LoadingLessonClient.renderTerrainBrief(graphics, width, height)) return;
        super.render(graphics, mouseX, mouseY, partialTick);
        callback.cancel();
    }
}
