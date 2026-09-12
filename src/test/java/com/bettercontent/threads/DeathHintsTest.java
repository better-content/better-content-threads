package com.bettercontent.threads;

import com.google.gson.JsonParser;
import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import org.junit.jupiter.api.Test;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

final class DeathHintsTest {
    private List<DeathHint> catalogue() throws Exception {
        return DeathHints.parse(JsonParser.parseString(Files.readString(Path.of(
            "src/main/resources/assets/better_content_threads/death_hints/catalogue.json"))).getAsJsonObject());
    }
    private DeathHint hint(String id, String pool, Set<String> context, Set<String> mods) {
        return new DeathHint(id, "test.concept", pool, "Prepare for your next journey.", context, mods, List.of("test authority"));
    }
    @Test void curatedPoolHasOneHundredNinetyTwoDistinctBoundedSourcedTips() throws Exception {
        var all = catalogue();
        assertEquals(192, all.size());
        assertEquals(80, all.stream().filter(h -> h.pool().equals("survival")).count());
        assertEquals(88, all.stream().filter(h -> h.pool().equals("discovery")).count());
        assertEquals(24, all.stream().filter(h -> h.pool().equals("teaser")).count());
        assertEquals(192, all.stream().map(DeathHint::text).distinct().count());
        for (String category : DeathHintContext.CATEGORIES) if (!category.equals("general"))
            assertTrue(all.stream().anyMatch(h -> h.contexts().contains(category)), category);
    }
    @Test void contextualAdvicePrefersUnseenThenFallsBackAcrossCategories() {
        var related = hint("related", "survival", Set.of("fall"), Set.of());
        var general = hint("general", "discovery", Set.of(), Set.of());
        var all = List.of(related, general);
        var state = DeathHintRotation.State.empty();
        var first = DeathHintRotation.select(all, "fall", state, m -> true, new Random(1));
        assertEquals(related, first.hint());
        state = DeathHintRotation.displayed(state, first, false);
        var second = DeathHintRotation.select(all, "fall", state, m -> true, new Random(1));
        assertEquals(general, second.hint());
        assertEquals(0, second.cycle());
    }
    @Test void unavailableModsNeverLeakAndAnEmptyPoolHasSafeAdvice() {
        var unavailable = hint("unavailable", "teaser", Set.of("fall"), Set.of("missing_mod"));
        var selection = DeathHintRotation.select(List.of(unavailable), "fall", DeathHintRotation.State.empty(), m -> false, new Random(1));
        assertEquals(DeathHints.FALLBACK, selection.hint());
        assertFalse(selection.contextualOpportunity());
    }
    @Test void everyEligibleTipIsShownBeforeAnyRepeatAcrossBothSurfaces() throws Exception {
        var state = DeathHintRotation.State.empty();
        var all = catalogue();
        var seen = new java.util.HashSet<String>();
        for (int i = 0; i < 192 * 3; i++) {
            boolean pause = i % 2 == 0;
            var chosen = DeathHintRotation.select(all, pause ? "general" : "fall", state, m -> true, new Random(i));
            if (chosen.cycle() > state.cycle()) {
                assertEquals(192, seen.size());
                seen.clear();
            }
            assertTrue(seen.add(chosen.hint().id()), "repeat before exhaustion: " + chosen.hint().id());
            state = DeathHintRotation.displayed(state, chosen, pause);
            state = DeathHintStore.decode(DeathHintStore.encode(state));
        }
        assertEquals(192, seen.size());
        assertEquals(2, state.cycle());
    }
    @Test void generalTeaserWeightIsApproximatelyOneInEight() throws Exception {
        int teasers = 0;
        var random = new Random(82);
        var all = catalogue();
        for (int i = 0; i < 8000; i++) if (DeathHintRotation.select(all, "general",
                DeathHintRotation.State.empty(), m -> true, random).hint().pool().equals("teaser")) teasers++;
        assertTrue(teasers > 900 && teasers < 1100, "teasers=" + teasers);
    }
    @Test void reservedLastUnseenTipDoesNotResetTheCycle() {
        var first = hint("first", "survival", Set.of("fire"), Set.of());
        var second = hint("second", "survival", Set.of("fire"), Set.of());
        var state = new DeathHintRotation.State(Set.of("first"), 0, 0, "", "first", "");
        assertNull(DeathHintRotation.select(List.of(first, second), "fire", state, m -> true, new Random(), Set.of("second")));
        assertEquals(second, DeathHintRotation.select(List.of(first, second), "fire", state, m -> true, new Random()).hint());
    }
    @Test void catalogueRejectsDuplicatesInvalidContextsAndUnknownBindingTokens() throws Exception {
        var json = JsonParser.parseString(Files.readString(Path.of(
            "src/main/resources/assets/better_content_threads/death_hints/catalogue.json"))).getAsJsonObject();
        json.getAsJsonArray("hints").add(json.getAsJsonArray("hints").get(0).deepCopy());
        assertThrows(IllegalArgumentException.class, () -> DeathHints.parse(json));
        assertThrows(IllegalArgumentException.class, () -> hint("bad", "survival", Set.of("guessed"), Set.of()));
        assertThrows(IllegalArgumentException.class, () -> new DeathHint("bad", "test.concept", "survival",
            "Press {guessed}.", Set.of(), Set.of(), List.of("test")));
    }
    @Test void actualModDamageIdsAndUnknownTypesClassifyWithoutTranslatedMessages() {
        assertEquals("thirst", classify("thirst:dehydrate"));
        assertEquals("cold", classify("cold_sweat:cold_scaling"));
        assertEquals("heat", classify("cold_sweat:hot_scaling"));
        assertEquals("general", classify("unknown:damage"));
        assertEquals("revival", classify("downed_player_revival:bled_out"));
        assertEquals("drowning", classify("minecraft:drown"));
        assertEquals("fall", DeathHintContext.classify("minecraft:fall", false, true, false, false, false, false));
        assertEquals("projectile", DeathHintContext.classify("minecraft:arrow", false, false, true, false, false, true));
    }
    private String classify(String type) { return DeathHintContext.classify(type, false, false, false, false, false, false); }
    @Test void bleedOutGiveUpAndFinishingRetainOriginalCauseOnce() {
        var memory = new DeathHintContext();
        var player = UUID.randomUUID();
        for (String terminal : List.of("downed_player_revival:bled_out", "downed_player_revival:finished")) {
            memory.downed(player, "fire");
            assertEquals("fire", memory.death(player, classify(terminal)));
            assertEquals("revival", memory.death(player, classify(terminal)));
        }
    }
    @Test void revivalDisconnectAndServerStopDiscardOriginalCause() {
        var memory = new DeathHintContext();
        var player = UUID.randomUUID();
        memory.downed(player, "fire");
        memory.clear(player);
        assertEquals("revival", memory.death(player, "revival"));
        memory.downed(player, "fall");
        memory.clear();
        assertEquals("revival", memory.death(player, "revival"));
        memory.downed(player, "general");
        assertEquals("revival", memory.death(player, "revival"));
    }
    @Test void packetRoundTripRejectsUnknownContext() {
        var packet = new ThreadNetwork.DeathContext(UUID.randomUUID(), "drowning");
        var buffer = new FriendlyByteBuf(Unpooled.buffer());
        try { packet.encode(buffer); assertEquals(packet, ThreadNetwork.DeathContext.decode(buffer)); }
        finally { buffer.release(); }
        assertThrows(IllegalArgumentException.class, () -> new ThreadNetwork.DeathContext(UUID.randomUUID(), "arbitrary"));
    }
    @Test void firstFrameFreezesContextAndHistoryRecordsOnlyOnce() {
        var session = new DeathHintSession();
        session.receive("fall", 100);
        assertEquals("fall", session.freeze(150));
        session.select(new DeathHintRotation.Selection(DeathHints.FALLBACK, true, 0));
        session.receive("fire", 200);
        session.select(new DeathHintRotation.Selection(hint("other", "discovery", Set.of(), Set.of()), false, 0));
        assertEquals(DeathHints.FALLBACK, session.selection().hint());
        assertTrue(session.record());
        assertFalse(session.record());
    }
    @Test void missingStaleOrLateContextFallsBack() {
        assertEquals("general", new DeathHintSession().freeze(100));
        var old = new DeathHintSession();
        old.receive("fire", 100);
        assertEquals("general", old.freeze(6000));
        var late = new DeathHintSession();
        assertEquals("general", late.freeze(100));
        late.receive("fire", 101);
        assertEquals("general", late.freeze(102));
    }
    @Test void layoutPreservesNativeControlsAndOmitsUnfittableText() {
        var normal = DeathHintLayout.calculate(427, 240, 176, 2, 9);
        assertTrue(normal.visible());
        assertTrue(normal.y() > 176);
        assertTrue(normal.y() + normal.height() <= 232);
        assertTrue(DeathHintLayout.calculate(320, 240, 176, 3, 9).visible());
        assertFalse(DeathHintLayout.calculate(320, 180, 161, 3, 9).visible());
        assertFalse(DeathHintLayout.calculate(427, 240, 176, 10, 9).visible());
        assertFalse(DeathHintLayout.calculate(427, 240, 230, 1, 9).visible());
    }
}
