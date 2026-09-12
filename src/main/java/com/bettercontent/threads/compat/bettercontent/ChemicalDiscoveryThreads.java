package com.bettercontent.threads.compat.bettercontent;

import com.bettercontent.latentchemlib.api.event.ChemicalOutcomeEvent;
import com.bettercontent.threads.OperationOwners;
import com.bettercontent.threads.ThreadSignals;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.ForgeRegistries;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** Causal chemical outcomes, with persistent ownership for placed radioactive material. */
public final class ChemicalDiscoveryThreads {
    @SubscribeEvent public static void committed(ChemicalOutcomeEvent event) {
        UUID actor = event.actor() != null ? event.actor() : OperationOwners.owner(event.device());
        String type, value;
        switch (event.kind()) {
            case GAS_RELEASED -> { type = "gas_pollution"; value = "released"; }
            case RADIOACTIVE_ACTIVATED -> {
                type = "radioactive_ore"; value = "activated";
                if (actor != null) Sources.get(event.level()).record(event.level(), event.pos(), actor);
            }
            case HEAT_ACCEPTED -> {
                type = "radiogenic_heat"; value = "accepted";
                actor = Sources.get(event.level()).owner(event.level(), event.pos());
                if (actor == null) actor = OperationOwners.owner(event.level().getBlockEntity(event.pos()));
            }
            default -> throw new IllegalStateException();
        }
        if (actor != null) ThreadSignals.emit(event.level().getServer(), actor, type, value,
            UUID.randomUUID().toString(), event.pos().toShortString());
    }
    @SubscribeEvent(priority = EventPriority.LOWEST) public static void broken(BlockEvent.BreakEvent event) {
        if (!event.isCanceled() && event.getLevel() instanceof ServerLevel level) Sources.get(level).remove(event.getPos());
    }
    public static final class Sources extends SavedData {
        private final Map<Long, Source> entries = new HashMap<>();
        static Sources get(ServerLevel level) {
            return level.getDataStorage().computeIfAbsent(Sources::load, Sources::new, "better_content_threads_chemical_sources");
        }
        void record(ServerLevel level, BlockPos pos, UUID owner) {
            record(pos, owner, ForgeRegistries.BLOCKS.getKey(level.getBlockState(pos).getBlock()).toString());
        }
        void record(BlockPos pos, UUID owner, String blockId) {
            entries.put(pos.asLong(), new Source(owner, blockId));
            setDirty();
        }
        UUID owner(ServerLevel level, BlockPos pos) {
            return owner(pos, ForgeRegistries.BLOCKS.getKey(level.getBlockState(pos).getBlock()).toString());
        }
        UUID owner(BlockPos pos, String blockId) {
            Source source = entries.get(pos.asLong());
            if (source == null) return null;
            if (!source.block().equals(blockId)) {
                remove(pos); return null;
            }
            return source.owner();
        }
        void remove(BlockPos pos) { if (entries.remove(pos.asLong()) != null) setDirty(); }
        @Override public CompoundTag save(CompoundTag tag) {
            ListTag list = new ListTag();
            entries.forEach((pos, source) -> { CompoundTag item = new CompoundTag(); item.putLong("pos", pos);
                item.putUUID("owner", source.owner()); item.putString("block", source.block()); list.add(item); });
            tag.put("sources", list); return tag;
        }
        static Sources load(CompoundTag tag) {
            Sources data = new Sources();
            for (Tag entry : tag.getList("sources", Tag.TAG_COMPOUND)) {
                CompoundTag item = (CompoundTag) entry;
                if (item.hasUUID("owner")) data.entries.put(item.getLong("pos"), new Source(item.getUUID("owner"), item.getString("block")));
            }
            return data;
        }
        private record Source(UUID owner, String block) {}
    }
}
