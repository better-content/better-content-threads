package com.bettercontent.threads;

import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.stream.IntStream;
import static org.junit.jupiter.api.Assertions.*;

final class HintLifecycleTest {
    private List<DeathHint> all = IntStream.range(0, 20).mapToObj(i -> new DeathHint("tip_" + i, "test.tip", "discovery",
        "Inspect your equipment before traveling.", Set.of(), Set.of(), List.of("test"))).toList();
    private final Random random = new Random(38);
    private DeathHintRotation.Selection pause(HintLifecycle life) { return life.pauseTip(all, m -> true, random); }
    private DeathHintRotation.Selection death(HintLifecycle life) { return life.deathTip(all, m -> true, random, 100); }

    @Test void pauseSelectionSurvivesReopenRespawnDisconnectAndRestart() {
        var life = new HintLifecycle(DeathHintRotation.State.empty());
        var initial = pause(life);
        assertSame(initial, pause(life));
        assertTrue(life.state().shown().isEmpty());
        life.displayed(initial, true);
        life.respawn();
        assertEquals(initial.hint(), pause(life).hint());
        life.disconnect();
        assertEquals(initial.hint(), pause(life).hint());
        var restarted = new HintLifecycle(DeathHintStore.decode(DeathHintStore.encode(life.state())));
        assertEquals(initial.hint(), pause(restarted).hint());
        assertEquals(Set.of(initial.hint().id()), restarted.state().shown());
    }
    @Test void contextPacketAndDeathScreenRotateExactlyOnceAndChooseSeparateTips() {
        var life = new HintLifecycle(DeathHintRotation.State.empty());
        var initial = pause(life);
        life.displayed(initial, true);
        assertTrue(life.death("fall", 100));
        assertEquals("", life.state().pauseId());
        var fatal = death(life);
        life.displayed(fatal, false);
        assertFalse(life.death("fire", 101));
        assertSame(fatal, death(life));
        life.respawn();
        var next = pause(life);
        assertNotEquals(initial.hint(), next.hint());
        assertNotEquals(fatal.hint(), next.hint());
        life.displayed(next, true);
        assertSame(next, pause(life));
        assertEquals(3, life.state().shown().size());
    }
    @Test void immediateRespawnStillInvalidatesPauseWithoutConsumingADeathTip() {
        var life = new HintLifecycle(DeathHintRotation.State.empty());
        var initial = pause(life);
        life.displayed(initial, true);
        life.death("fall", 100);
        life.respawn();
        var next = pause(life);
        assertNotEquals(initial.hint(), next.hint());
        assertEquals(1, life.state().shown().size());
    }
    @Test void unrenderedTipsRemainAvailableAfterAnotherDeath() {
        var life = new HintLifecycle(DeathHintRotation.State.empty());
        var unseen = pause(life);
        life.death("fall", 100);
        var fatal = death(life);
        life.respawn();
        assertFalse(life.state().shown().contains(unseen.hint().id()));
        assertFalse(life.state().shown().contains(fatal.hint().id()));
        assertEquals("", life.state().pauseId());
    }
    @Test void exhaustionCommitsOnlyWhenNewCycleTipRenders() {
        all = all.subList(0, 3);
        var state = new DeathHintRotation.State(Set.of("tip_0", "tip_1", "tip_2"), 0, 0, "", "tip_1", "tip_2");
        var life = new HintLifecycle(state);
        var selected = pause(life);
        assertEquals("tip_0", selected.hint().id());
        assertEquals(1, selected.cycle());
        assertEquals(state, life.state());
        assertSame(selected, pause(life));
        life.displayed(selected, true);
        assertEquals(1, life.state().cycle());
        assertEquals(Set.of("tip_0"), life.state().shown());
    }
    @Test void resourceReloadRetainsValidPauseAndMakesAddedTipsUnseen() {
        var life = new HintLifecycle(DeathHintRotation.State.empty());
        var current = pause(life);
        life.displayed(current, true);
        var replacement = new DeathHint(current.hint().id(), "test.tip", "discovery", "Updated instructions.", Set.of(), Set.of(), List.of("test"));
        assertEquals(replacement, life.pauseTip(List.of(replacement), m -> true, random).hint());
        var newTip = all.stream().filter(h -> !h.id().equals(current.hint().id())).findFirst().orElseThrow();
        var next = DeathHintRotation.select(List.of(replacement, newTip), "general", life.state(), m -> true, random);
        assertEquals(newTip, next.hint());
        assertEquals(0, next.cycle());
    }
    @Test void unavailableTipsDoNotBlockCycleAndSingleEntryCanRepeatAfterExhaustion() {
        all = all.subList(0, 1);
        var life = new HintLifecycle(DeathHintRotation.State.empty());
        var fatal = death(life);
        life.displayed(fatal, false);
        var selected = pause(life);
        assertNotNull(selected);
        assertEquals(1, selected.cycle());
        life.displayed(selected, true);
        var unavailable = new DeathHint("missing", "test.tip", "discovery", "Missing system.", Set.of(), Set.of("missing"), List.of("test"));
        assertEquals(2, DeathHintRotation.select(List.of(all.get(0), unavailable), "general", life.state(), m -> false, random).cycle());
    }
    @Test void malformedCatalogueFallbackDoesNotDestroyHistoryOrPauseChoice() {
        var life = new HintLifecycle(DeathHintRotation.State.empty());
        var tip = pause(life);
        life.displayed(tip, true);
        var before = life.state();
        var fallback = life.pauseTip(List.of(DeathHints.FALLBACK), m -> true, random);
        life.displayed(fallback, true);
        assertEquals(before, life.state());
        assertEquals(tip.hint(), pause(life).hint());
    }
    @Test void versionOneHistoryMigratesAllKnownIds() {
        var state = DeathHintStore.decode(JsonParser.parseString("{\"schema\":1,\"recent\":[\"tip_1\",\"tip_2\"],\"contextual_deaths\":9}").getAsJsonObject());
        assertEquals(Set.of("tip_1", "tip_2"), state.shown());
        assertEquals("tip_2", state.lastDeathId());
        assertEquals(9, state.contextualDeaths());
        assertEquals(2, DeathHintStore.encode(state).get("schema").getAsInt());
    }
    @Test void repeatedDeathsAndRestartsExhaustEachSharedCycle() {
        var life = new HintLifecycle(DeathHintRotation.State.empty());
        var seen = new java.util.HashSet<String>();
        for (int i = 0; i < 60; i++) {
            boolean isPause = i % 2 == 0;
            if (!isPause) life.death("fall", 100);
            var selected = isPause ? pause(life) : death(life);
            if (selected.cycle() > life.state().cycle()) {
                assertEquals(all.size(), seen.size());
                seen.clear();
            }
            assertTrue(seen.add(selected.hint().id()), "Repeated " + selected.hint().id());
            life.displayed(selected, isPause);
            if (!isPause) life.respawn();
            life = new HintLifecycle(DeathHintStore.decode(DeathHintStore.encode(life.state())));
        }
        assertEquals(all.size(), seen.size());
        assertEquals(2, life.state().cycle());
    }
    @Test void unrenderedNextCycleSelectionDoesNotPersistAnUncommittedCycle() {
        all = all.subList(0, 3);
        var exhausted = new DeathHintRotation.State(Set.of("tip_0", "tip_1", "tip_2"), 4, 0, "", "tip_1", "tip_2");
        var life = new HintLifecycle(exhausted);
        assertEquals(5, pause(life).cycle());
        var restarted = new HintLifecycle(DeathHintStore.decode(DeathHintStore.encode(life.state())));
        var selected = pause(restarted);
        assertEquals(5, selected.cycle());
        restarted.displayed(selected, true);
        assertEquals(Set.of("tip_0"), restarted.state().shown());
        assertEquals(5, restarted.state().cycle());
    }
    @Test void atomicHistoryFileRoundTrip(@TempDir Path directory) throws Exception {
        var life = new HintLifecycle(DeathHintRotation.State.empty());
        life.displayed(pause(life), true);
        Path file = directory.resolve("history.json");
        DeathHintStore.write(file, life.state());
        assertEquals(life.state(), DeathHintStore.decode(JsonParser.parseString(Files.readString(file)).getAsJsonObject()));
        try (var files = Files.list(directory)) { assertEquals(1, files.count()); }
    }
    @Test void compactPauseReservesThreeLinesWithoutShrinkingButtons() {
        var layout = PauseHintLayout.calculate(240, 62, 202, 3, 9);
        assertTrue(layout.fits());
        assertEquals(26, layout.shift());
        assertEquals(12, layout.titleY());
        assertFalse(PauseHintLayout.calculate(180, 50, 190, 4, 9).fits());
        assertEquals(0, PauseHintLayout.calculate(360, 80, 220, 3, 9).shift());
    }
}
