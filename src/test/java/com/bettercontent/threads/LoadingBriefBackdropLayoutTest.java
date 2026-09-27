package com.bettercontent.threads;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class LoadingBriefBackdropLayoutTest {
    @Test
    void artworkUsesLargestCenteredTwoToOneContainRectangle() {
        for (var size : sizes()) {
            var layout = LoadingBriefBackdropLayout.calculate(size[0], size[1], true);
            int expectedWidth = Math.min(size[0], size[1] * 2);
            expectedWidth -= Math.floorMod(expectedWidth, 2);
            assertEquals(expectedWidth, layout.artWidth());
            assertEquals(layout.artWidth(), layout.artHeight() * 2);
            assertTrue(layout.artX() >= 0);
            assertTrue(layout.artY() >= 0);
            assertTrue(layout.artX() + layout.artWidth() <= size[0]);
            assertTrue(layout.artY() + layout.artHeight() <= size[1]);
            assertTrue(Math.abs(layout.artX() * 2 + layout.artWidth() - size[0]) <= 1);
            assertTrue(Math.abs(layout.artY() * 2 + layout.artHeight() - size[1]) <= 1);
        }
    }

    @Test
    void captionAndControlsRemainSeparatedAtSupportedSizes() {
        for (var size : sizes()) {
            for (boolean progress : List.of(false, true)) {
                var layout = LoadingBriefBackdropLayout.calculate(size[0], size[1], progress);
                assertTrue(layout.captionX() >= 0);
                assertTrue(layout.captionX() + layout.captionWidth() <= size[0]);
                assertTrue(layout.captionY() >= (progress ? layout.barY() + 28 : 26));
                assertTrue(layout.captionY() + layout.captionHeight() < layout.controlsY());
                assertTrue(layout.controlsY() + 20 <= size[1]);
            }
        }
    }

    @Test
    void nativeSecondControlRowReceivesReservedSpace() {
        for (var size : sizes()) {
            var oneRow = LoadingBriefBackdropLayout.calculate(size[0], size[1], false, 1);
            var twoRows = LoadingBriefBackdropLayout.calculate(size[0], size[1], false, 2);
            assertEquals(oneRow.controlsY(), twoRows.controlsY());
            assertEquals(oneRow.captionY() + oneRow.captionHeight() - 24,
                twoRows.captionY() + twoRows.captionHeight());
        }
    }

    private static List<int[]> sizes() {
        return List.of(new int[]{320, 240}, new int[]{427, 240}, new int[]{854, 480},
            new int[]{1920, 1080});
    }
}
