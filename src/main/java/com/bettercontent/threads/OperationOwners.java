package com.bettercontent.threads;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import java.util.UUID;

/** Ownership is persisted on the native device. Observation and menu access never transfer it. */
public final class OperationOwners {
    private static final String OWNER = "BetterContentThreadsOperator";
    private OperationOwners() {}
    @SubscribeEvent public static void placed(BlockEvent.EntityPlaceEvent event) {
        if (!event.isCanceled() && event.getEntity() instanceof ServerPlayer player)
            configure(event.getLevel().getBlockEntity(event.getPos()), player.getUUID());
    }
    public static void configure(BlockEntity device, UUID player) {
        if (device == null || player == null || !(device.getLevel() instanceof ServerLevel)) return;
        device.getPersistentData().putUUID(OWNER, player);
        device.setChanged();
    }
    public static UUID owner(BlockEntity device) {
        return device != null && device.getPersistentData().hasUUID(OWNER) ? device.getPersistentData().getUUID(OWNER) : null;
    }
    public static void completed(BlockEntity device, String type, String value, String context) {
        UUID owner = owner(device);
        if (owner == null || !(device.getLevel() instanceof ServerLevel level)) return;
        String operation = UUID.randomUUID().toString();
        ThreadSignals.emit(level.getServer(), owner, type, value, operation, context);
        if (net.minecraftforge.fml.ModList.get().isLoaded("arcane_chunk_loaders"))
            com.bettercontent.threads.compat.bettercontent.ArcaneChunkLoaderThreads.workCompleted(device, owner, operation, context);
    }
}
