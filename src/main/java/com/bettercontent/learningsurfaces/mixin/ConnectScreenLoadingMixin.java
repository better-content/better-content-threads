package com.bettercontent.learningsurfaces.mixin;

import com.bettercontent.learningsurfaces.LoadingLessonClient;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.ConnectScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ConnectScreen.class)
public abstract class ConnectScreenLoadingMixin extends Screen {
    @Shadow private Component status;

    protected ConnectScreenLoadingMixin(Component title) {
        super(title);
    }

    // Let vanilla update its connection narration, then own the visible presentation.
    @Inject(method = "render", at = @At(value = "INVOKE", target =
        "Lnet/minecraft/client/gui/GuiGraphics;drawCenteredString(Lnet/minecraft/client/gui/Font;Lnet/minecraft/network/chat/Component;III)V"),
        cancellable = true)
    private void learningSurfaces$render(GuiGraphics graphics, int mouseX, int mouseY,
                                             float partialTick, CallbackInfo callback) {
        if (!LoadingLessonClient.renderJoiningBrief(graphics, status, width, height)) return;
        super.render(graphics, mouseX, mouseY, partialTick);
        callback.cancel();
    }
}
