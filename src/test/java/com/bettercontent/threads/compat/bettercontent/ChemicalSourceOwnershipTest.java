package com.bettercontent.threads.compat.bettercontent;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import org.junit.jupiter.api.Test;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

class ChemicalSourceOwnershipTest {
    @Test void attributionSurvivesSaveAndRejectsReplacedMaterial() {
        UUID placer = UUID.randomUUID();
        BlockPos ore = new BlockPos(12, 47, -23), other = ore.east();
        var data = new ChemicalDiscoveryThreads.Sources();
        data.record(ore, placer, "realistic_ores:uranium_host");
        data.record(other, placer, "realistic_ores:thorium_host");
        var restored = ChemicalDiscoveryThreads.Sources.load(data.save(new CompoundTag()));
        assertEquals(placer, restored.owner(ore, "realistic_ores:uranium_host"));
        assertNull(restored.owner(ore, "minecraft:stone"));
        assertTrue(restored.isDirty());
        assertNull(restored.owner(ore, "realistic_ores:uranium_host"), "Stale attribution is removed, not merely hidden");
        assertEquals(placer, restored.owner(other, "realistic_ores:thorium_host"), "Another source keeps its owner");
        var nextRestart = ChemicalDiscoveryThreads.Sources.load(restored.save(new CompoundTag()));
        assertNull(nextRestart.owner(ore, "realistic_ores:uranium_host"));
        assertEquals(placer, nextRestart.owner(other, "realistic_ores:thorium_host"));
    }

    @Test void NewPlacementReplacesPriorOwnerAndBreakClearsIt() {
        BlockPos pos = new BlockPos(-10, 20, 30);
        UUID first = UUID.randomUUID(), second = UUID.randomUUID();
        var data = new ChemicalDiscoveryThreads.Sources();
        data.record(pos, first, "realistic_ores:uranium_host");
        data.record(pos, second, "realistic_ores:uranium_host");
        var restored = ChemicalDiscoveryThreads.Sources.load(data.save(new CompoundTag()));
        assertEquals(second, restored.owner(pos, "realistic_ores:uranium_host"));
        restored.remove(pos);
        assertNull(ChemicalDiscoveryThreads.Sources.load(restored.save(new CompoundTag()))
            .owner(pos, "realistic_ores:uranium_host"));
    }
}
