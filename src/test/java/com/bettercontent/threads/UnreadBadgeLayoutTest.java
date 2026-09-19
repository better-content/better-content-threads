package com.bettercontent.threads;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class UnreadBadgeLayoutTest {
    @Test
    void reservesTheWiderThreadsLabelInsteadOfOnlyTheKeycap() {
        var layout = UnreadBadgeLayout.calculate(427, 14, 42);
        assertEquals(42, layout.contentWidth());
        assertTrue(layout.showLabel());
        assertTrue(layout.right() <= 421);
    }

    @Test
    void staysInsideEachSupportedGuiWidthForShortAndLongBindings() {
        for (int width : List.of(320, 427, 854, 1920)) {
            for (int keyWidth : List.of(14, 54)) {
                var layout = UnreadBadgeLayout.calculate(width, keyWidth, 42);
                assertTrue(layout.plateX() >= 0);
                assertTrue(layout.right() <= width - 6);
                assertTrue(layout.showLabel());
            }
        }
    }

    @Test
    void suppressesTheLabelBeforeItCanOverflowAnUnsupportedNarrowViewport() {
        var layout = UnreadBadgeLayout.calculate(64, 14, 42);
        assertEquals(35, layout.contentWidth());
        assertTrue(layout.right() <= 58);
        assertTrue(!layout.showLabel());
    }
}
