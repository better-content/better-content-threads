package com.bettercontent.betterdiscoveryguides.gametest;

import com.bettercontent.betterdiscoveryguides.ThreadDefinitions;
import com.bettercontent.betterdiscoveryguides.ThreadPlayerState;
import com.bettercontent.betterdiscoveryguides.ThreadSignals;
import com.mojang.authlib.GameProfile;
import java.util.UUID;
import java.util.function.Consumer;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraft.gametest.framework.GameTest;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

/** Core persistence and duplicate contracts against the promoted card roster. */
@GameTestHolder("better_discovery_guides")
@PrefixGameTestTemplate(false)
public final class ThreadGameTests {
    private static final String CARD = "manual_work";
    private static final String EPISODE = "gametest:manual-work-a";

    @GameTest(template = "empty") public static void committed_outcome_immediately_discovers(GameTestHelper h) {
        withPlayer(h, p -> {
            ThreadSignals.emit(p, "create_manual_complete", "hand_crank", EPISODE);
            var state = ThreadPlayerState.get(p);
            h.assertTrue(state.known.contains(CARD) && state.unread.contains(CARD) && state.discovered.contains(CARD),
                "One committed manual work must immediately teach the complete card");
            h.assertTrue(state.generationCounts.getOrDefault(CARD, 0) == 1, "Discovery must credit once");
        });
    }

    @GameTest(template = "empty") public static void setup_and_invalid_evidence_do_not_discover(GameTestHelper h) {
        withPlayer(h, p -> {
            ThreadSignals.emit(p, "create_manual_complete", "wrong_machine", EPISODE);
            ThreadSignals.emit(p, "create_manual_complete", "hand_crank", null);
            h.assertFalse(ThreadPlayerState.get(p).known.contains(CARD), "Wrong operation and uncorrelated claims cannot discover");
        });
    }

    @GameTest(template = "empty") public static void duplicate_delivery_counts_once(GameTestHelper h) {
        withPlayer(h, p -> {
            ThreadSignals.emit(p, "create_manual_complete", "hand_crank", EPISODE);
            ThreadSignals.emit(p, "create_manual_complete", "hand_crank", EPISODE);
            ThreadSignals.emit(p, "create_manual_complete", "hand_crank", "gametest:manual-work-b");
            h.assertTrue(ThreadPlayerState.get(p).generationCounts.getOrDefault(CARD, 0) == 1,
                "Repeated delivery in one generation must not inflate history");
        });
    }

    @GameTest(template = "empty") public static void offline_success_survives_reload(GameTestHelper h) {
        withPlayer(h, p -> {
            ThreadSignals.emit(p.server, p.getUUID(), "create_manual_complete", "hand_crank", EPISODE, "Hand crank work completed");
            var state = ThreadPlayerState.get(p);
            h.assertTrue(state.pendingNotices.contains(CARD), "Offline notice must be durable");
            state.markRead(CARD);
            state.save(p);
            ThreadPlayerState.forget(p);
            var restored = ThreadPlayerState.get(p);
            h.assertTrue(restored.known.contains(CARD) && !restored.unread.contains(CARD)
                && restored.pendingNotices.contains(CARD), "History and pending notice must survive cache eviction");
            h.assertTrue("Hand crank work completed".equals(restored.contexts.get(CARD)), "Context must survive reload");
        });
    }

    private static void withPlayer(GameTestHelper h, Consumer<ServerPlayer> scenario) {
        h.assertTrue(ThreadDefinitions.INSTANCE.contains(CARD), "Packaged card catalogue must load");
        var player = FakePlayerFactory.get(h.getLevel(), new GameProfile(UUID.randomUUID(), "surface-test"));
        try { scenario.accept(player); h.succeed(); }
        finally { ThreadPlayerState.forget(player); }
    }
}
