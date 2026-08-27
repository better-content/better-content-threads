package com.bettercontent.threads.compat;

import com.bettercontent.threads.ThreadSignals;
import me.desht.pneumaticcraft.common.block.entity.AirCompressorBlockEntity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/** Version-pinned PneumaticCraft 6.0.22 native pressure episode. */
public final class PneumaticThreads {
    private PneumaticThreads() {}

    @SubscribeEvent
    public static void useWorkingCompressor(PlayerInteractEvent.RightClickBlock event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (!(player.level().getBlockEntity(event.getPos()) instanceof AirCompressorBlockEntity compressor)) return;
        if (!compressor.isActive() || compressor.getPressure() <= 0.1f) return;
        String token = player.getUUID() + ":pressure:" + event.getPos().asLong() + ":" + player.server.getTickCount();
        ThreadSignals.emit(player, "pressure_working", "air_compressor", token);
    }
}
