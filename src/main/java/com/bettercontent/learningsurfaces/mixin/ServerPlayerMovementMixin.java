package com.bettercontent.learningsurfaces.mixin;

import com.bettercontent.learningsurfaces.FirstUseCues;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Observe accepted movement packets for nearby first-use teaching cues. */
@Mixin(ServerGamePacketListenerImpl.class)
public abstract class ServerPlayerMovementMixin {
    @Shadow public net.minecraft.server.level.ServerPlayer player;

    @Inject(method = "handleMovePlayer", at = @At("TAIL"))
    private void learningSurfaces$afterMove(ServerboundMovePlayerPacket packet, CallbackInfo callback) {
        FirstUseCues.moved(player);
    }
}
