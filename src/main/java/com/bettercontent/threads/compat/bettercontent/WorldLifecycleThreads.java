package com.bettercontent.threads.compat.bettercontent;

import com.bettercontent.threads.ThreadSignals;
import com.bettercontent.worldlifecyclemanager.PrestigeService;
import com.bettercontent.worldlifecyclemanager.api.LineagePlayerDataApi;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.io.IOException;
import java.util.UUID;

/** Maps World Lifecycle Manager's durable lineage actions into correlated Thread evidence. */
public final class WorldLifecycleThreads {
    private WorldLifecycleThreads() {}

    /** Player visit history is itself stored in this lineage; a new visitor is not a successor. */
    public static boolean verifySuccessor(net.minecraft.server.level.ServerPlayer player) {
        try {
            ResourceLocation key = new ResourceLocation("better_content_threads", "discovery_visit");
            CompoundTag previous = readPlayerData(player.server, key, player.getUUID());
            long current = generation(player.server);
            String lineage = lineageId(player.server);
            boolean successor = previous.contains("generation") && lineage.equals(previous.getString("lineage"))
                && (current > previous.getLong("generation") || previous.getBoolean("pending"));
            CompoundTag visit = new CompoundTag(); visit.putString("lineage", lineage); visit.putLong("generation", current); visit.putBoolean("pending", successor);
            writePlayerData(player.server, key, player.getUUID(), visit);
            return successor;
        } catch (IOException failure) {
            throw new IllegalStateException("Cannot verify successor lineage", failure);
        }
    }

    public static void successorRecorded(net.minecraft.server.level.ServerPlayer player) {
        try {
            ResourceLocation key = new ResourceLocation("better_content_threads", "discovery_visit");
            CompoundTag visit = readPlayerData(player.server, key, player.getUUID());
            visit.putBoolean("pending", false); writePlayerData(player.server, key, player.getUUID(), visit);
        } catch (IOException failure) { throw new IllegalStateException("Cannot acknowledge successor discovery", failure); }
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
