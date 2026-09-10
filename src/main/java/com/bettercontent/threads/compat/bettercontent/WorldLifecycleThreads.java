package com.bettercontent.threads.compat.bettercontent;

import com.bettercontent.threads.ThreadSignals;
import com.bettercontent.worldlifecyclemanager.PrestigeService;
import com.bettercontent.worldlifecyclemanager.api.LineagePlayerDataApi;
import com.bettercontent.worldlifecyclemanager.api.event.SchematicPublishedEvent;
import com.bettercontent.worldlifecyclemanager.api.event.WorldCondenserAccessedEvent;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.io.IOException;
import java.util.UUID;

/** Maps World Lifecycle Manager's durable lineage actions into correlated Thread evidence. */
public final class WorldLifecycleThreads {
    private WorldLifecycleThreads() {}

    @SubscribeEvent
    public static void schematicPublished(SchematicPublishedEvent event) {
        ThreadSignals.emit(event.getPlayer(), "schematic_capture", "substantial", event.getEpisodeId());
        ThreadSignals.emit(event.getPlayer(), "schematic_publish", "correlated", event.getEpisodeId());
    }

    @SubscribeEvent
    public static void condenserAccessed(WorldCondenserAccessedEvent event) {
        ThreadSignals.emit(event.getPlayer(), "condenser", "formed", event.getEpisodeId());
    }

    public static long generation(MinecraftServer server) throws IOException {
        return PrestigeService.lineage(server).generation();
    }

    public static String lineageId(MinecraftServer server) throws IOException {
        return PrestigeService.lineage(server).lineageId();
    }

    public static CompoundTag readPlayerData(
        MinecraftServer server,
        ResourceLocation key,
        UUID playerId
    ) throws IOException {
        return LineagePlayerDataApi.read(server, key, playerId);
    }

    public static void writePlayerData(
        MinecraftServer server,
        ResourceLocation key,
        UUID playerId,
        CompoundTag value
    ) throws IOException {
        LineagePlayerDataApi.write(server, key, playerId, value);
    }
}
