package com.bettercontent.threads;

import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

final class ThreadCardPacketTest {
    private ThreadNetwork.Card card(boolean known, String rule, String action) {
        return new ThreadNetwork.Card("world_remembers", "world.traces", "Player Traces",
                "world", 2, "control", "better_content_threads:textures/gui/threads/world_remembers.png",
                known, known, known, false, rule, action, "", "", 2, 1, 3, "Traces inspected: 2");
    }

    @Test void cardRoundTripsRuleActionAndHistoryWithoutNarrativeFields() {
        var original = card(true, "Player traces record movement.", "Inspect nearby traces.");
        var buffer = new FriendlyByteBuf(Unpooled.buffer());
        try {
            original.encode(buffer);
            assertEquals(original, ThreadNetwork.Card.decode(buffer));
            assertEquals(0, buffer.readableBytes());
        } finally {
            buffer.release();
        }
    }

    @Test void unknownCardsRejectTeachingCopyAndRoundTripEmptyFields() {
        assertThrows(IllegalArgumentException.class, () -> card(false, "A rule.", ""));
        assertThrows(IllegalArgumentException.class, () -> card(false, "", "An action."));
        var original = card(false, "", "");
        var buffer = new FriendlyByteBuf(Unpooled.buffer());
        try {
            original.encode(buffer);
            assertEquals(original, ThreadNetwork.Card.decode(buffer));
            assertEquals(0, buffer.readableBytes());
        } finally {
            buffer.release();
        }
    }
}
