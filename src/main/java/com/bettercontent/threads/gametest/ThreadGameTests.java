package com.bettercontent.threads.gametest;

import com.bettercontent.threads.ThreadDefinitions;
import com.bettercontent.threads.ThreadPlayerState;
import com.bettercontent.threads.ThreadSignals;
import com.mojang.authlib.GameProfile;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.UUID;
import java.util.function.Consumer;

@GameTestHolder("better_content_threads")
@PrefixGameTestTemplate(false)
public final class ThreadGameTests {
    private static final String THREAD = "stone_makes_promises";
    private static final String EPISODE = "gametest:deposit-a";

    @GameTest(template = "empty")
    public static void signals_reveal_and_complete(GameTestHelper helper) {
        withPlayer(helper, player -> {
            reveal(player);
            var state = ThreadPlayerState.get(player);
            helper.assertTrue(state.known.contains(THREAD) && state.active.contains(THREAD)
                && state.unread.contains(THREAD), "Reveal must activate a known unread thread");
            helper.assertFalse(state.completed.contains(THREAD), "Revelation alone must not complete the thread");
            complete(player, EPISODE);
            assertCompletedOnce(helper, player);
        });
    }

    @GameTest(template = "empty")
    public static void wrong_correlation_does_not_complete(GameTestHelper helper) {
        withPlayer(helper, player -> {
            reveal(player);
            complete(player, "gametest:other-deposit");
            var state = ThreadPlayerState.get(player);
            helper.assertFalse(state.completed.contains(THREAD), "A different deposit must not complete the episode");
            helper.assertTrue(state.completionCounts.getOrDefault(THREAD, 0) == 0,
                "Rejected correlation must not increment history");
            helper.assertTrue(EPISODE.equals(ThreadSignals.activeCorrelation(player, THREAD)),
                "Rejected completion must preserve the original correlation");
            complete(player, EPISODE);
            assertCompletedOnce(helper, player);
        });
    }

    @GameTest(template = "empty")
    public static void repeated_signals_complete_once(GameTestHelper helper) {
        withPlayer(helper, player -> {
            reveal(player);
            reveal(player);
            complete(player, EPISODE);
            complete(player, EPISODE);
            reveal(player);
            complete(player, EPISODE);
            assertCompletedOnce(helper, player);
        });
    }

    @GameTest(template = "empty")
    public static void player_state_survives_reload(GameTestHelper helper) {
        withPlayer(helper, player -> {
            ThreadPlayerState.get(player).enterGeneration(7);
            reveal(player);
            var beforeReload = ThreadPlayerState.get(player);
            ThreadPlayerState.forget(player);
            var activeReload = ThreadPlayerState.get(player);
            helper.assertTrue(activeReload != beforeReload && activeReload.generation == 7
                && EPISODE.equals(activeReload.correlations.get(THREAD)),
                "Player persisted NBT must restore generation and unfinished correlation without the cache");
            complete(player, EPISODE);
            ThreadPlayerState.forget(player);
            assertCompletedOnce(helper, player);
            var finished = ThreadPlayerState.get(player);
            helper.assertTrue(finished.firstGeneration.get(THREAD) == 7 && finished.lastGeneration.get(THREAD) == 7,
                "Completion generation history must survive reload");
            helper.assertTrue(finished.known.contains(THREAD) && finished.unread.contains(THREAD),
                "Knowledge and unread state must survive reload");
        });
    }

    private static void withPlayer(GameTestHelper helper, Consumer<ServerPlayer> scenario) {
        helper.assertTrue(ThreadDefinitions.INSTANCE.get(THREAD) != null,
            "The production catalogue must be loaded by the server resource reload");
        var player = FakePlayerFactory.get(helper.getLevel(), new GameProfile(UUID.randomUUID(), "thread-test"));
        try {
            scenario.accept(player);
            helper.succeed();
        } finally {
            ThreadPlayerState.forget(player);
        }
    }

    private static void reveal(ServerPlayer player) {
        ThreadSignals.emit(player, "deposit_read", "gametest-deposit", EPISODE);
    }

    private static void complete(ServerPlayer player, String correlation) {
        ThreadSignals.emit(player, "deposit_extract", "gametest-deposit", correlation);
    }

    private static void assertCompletedOnce(GameTestHelper helper, ServerPlayer player) {
        var state = ThreadPlayerState.get(player);
        helper.assertTrue(state.completed.contains(THREAD) && state.completionCounts.getOrDefault(THREAD, 0) == 1,
            "The episode must complete exactly once");
        String route = ThreadDefinitions.INSTANCE.get(THREAD).completionRoutes().get(0).label();
        helper.assertTrue(state.routeCounts.get(THREAD).getOrDefault(route, 0) == 1,
            "The selected route history must increment exactly once");
        helper.assertFalse(state.correlations.containsKey(THREAD), "Completion must consume the active correlation");
    }
}
