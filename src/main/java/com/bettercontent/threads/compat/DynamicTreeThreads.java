package com.bettercontent.threads.compat;

import com.bettercontent.threads.ThreadSignals;
import com.ferreusveritas.dynamictrees.api.event.TransitionSaplingToTreeEvent;
import com.ferreusveritas.dynamictrees.block.branch.BranchBlock;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/** Version-pinned Dynamic Trees evidence for a mature felling and compatible renewal. */
public final class DynamicTreeThreads {
    private static final String ROOT = "BetterContentThreadsForestEpisode";
    private static final int MATURE_RADIUS = 8;
    private static final double REPLANT_RADIUS_SQUARED = 12.0 * 12.0;

    private DynamicTreeThreads() {}

    @SubscribeEvent
    public static void onBranchBroken(BlockEvent.BreakEvent event) {
        if (!(event.getPlayer() instanceof ServerPlayer player)) return;
        if (!(event.getState().getBlock() instanceof BranchBlock branch)) return;
        if (branch.getRadius(event.getState()) < MATURE_RADIUS) return;
        String family = branch.getFamily().getRegistryName().toString();
        String token = player.getUUID() + ":forest:" + player.server.getTickCount();
        CompoundTag persisted = player.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG);
        CompoundTag episode = new CompoundTag();
        episode.putString("dimension", player.serverLevel().dimension().location().toString());
        episode.putLong("opening", event.getPos().asLong());
        episode.putString("family", family);
        episode.putString("token", token);
        persisted.put(ROOT, episode);
        player.getPersistentData().put(Player.PERSISTED_NBT_TAG, persisted);
        ThreadSignals.emit(player, "tree_felled", "natural_mature", token);
    }

    @SubscribeEvent
    public static void onSaplingEstablished(TransitionSaplingToTreeEvent event) {
        if (event.getLevel().isClientSide) return;
        String family = event.getSpecies().getFamily().getRegistryName().toString();
        String dimension = event.getLevel().dimension().location().toString();
        for (Player candidate : event.getLevel().players()) {
            if (!(candidate instanceof ServerPlayer player)) continue;
            CompoundTag persisted = player.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG);
            CompoundTag episode = persisted.getCompound(ROOT);
            String token = episode.getString("token");
            if (!dimension.equals(episode.getString("dimension")) || !family.equals(episode.getString("family"))) continue;
            if (event.getPos().distSqr(net.minecraft.core.BlockPos.of(episode.getLong("opening"))) > REPLANT_RADIUS_SQUARED) continue;
            if (token.isBlank() || token.length() > 128) continue;
            ThreadSignals.emit(player, "tree_replant", "compatible", token);
            persisted.remove(ROOT);
            player.getPersistentData().put(Player.PERSISTED_NBT_TAG, persisted);
        }
    }
}
