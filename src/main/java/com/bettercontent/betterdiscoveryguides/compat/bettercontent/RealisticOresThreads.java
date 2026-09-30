package com.bettercontent.betterdiscoveryguides.compat.bettercontent;

import com.bettercontent.betteroregeology.api.event.DepositSurveyEvent;
import com.bettercontent.betterdiscoveryguides.ThreadSignals;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.ForgeRegistries;
import java.util.Map;
import java.util.UUID;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/** Maps Realistic Ores' persisted sample-to-extraction survey episodes into Thread evidence. */
public final class RealisticOresThreads {
    private RealisticOresThreads() {}
    private record Extraction(String family, long tick) {}
    private static final List<String> FAMILIES = List.of("black_shale", "brassroot", "coal_measures",
        "copper_bloom", "evaporite_beds", "hotstone", "ironstone", "tin_quartz");
    private static final Map<UUID, Extraction> RECENT_EXTRACTIONS = new ConcurrentHashMap<>();

    @SubscribeEvent
    public static void depositSurveyProgressed(DepositSurveyEvent event) {
        ThreadEvidence evidence = evidence(event.kind(), event.family());
        ThreadSignals.emit(event.player(), evidence.type(), evidence.value(), event.episodeId());
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void brokeDeposit(BlockEvent.BreakEvent event) {
        if (event.isCanceled() || !(event.getPlayer() instanceof ServerPlayer player)) return;
        var id = ForgeRegistries.BLOCKS.getKey(event.getState().getBlock());
        if (id == null) return;
        String block = id.toString();
        if ((block.startsWith("better_ore_geology:") || block.startsWith("excavated_variants:"))
                && !block.contains("surface_sample"))
            FAMILIES.stream().filter(block::endsWith).findFirst().ifPresent(family ->
                RECENT_EXTRACTIONS.put(player.getUUID(), new Extraction(family, player.server.getTickCount())));
    }

    @SubscribeEvent public static void pickedUpChunk(EntityItemPickupEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        var id = ForgeRegistries.ITEMS.getKey(event.getItem().getItem().getItem());
        Extraction extraction = RECENT_EXTRACTIONS.get(player.getUUID());
        if (id != null && extraction != null
                && id.toString().equals("better_ore_geology:ore_chunk_" + extraction.family())
                && player.server.getTickCount() - extraction.tick() <= 100) {
            ThreadSignals.emit(player, "geological_chunk", "acquired", player.getUUID() + ":chunk:" + extraction.tick());
            RECENT_EXTRACTIONS.remove(player.getUUID());
        }
    }

    @SubscribeEvent public static void logout(PlayerEvent.PlayerLoggedOutEvent event) {
        RECENT_EXTRACTIONS.remove(event.getEntity().getUUID());
    }

    static ThreadEvidence evidence(DepositSurveyEvent.Kind kind, String family) {
        return switch (kind) {
            case SAMPLE_READ -> new ThreadEvidence("deposit_read", family);
            case DEPOSIT_EXTRACTED -> new ThreadEvidence("deposit_extract", family);
        };
    }
}
