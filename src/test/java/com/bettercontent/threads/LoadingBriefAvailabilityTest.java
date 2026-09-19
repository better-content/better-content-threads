package com.bettercontent.threads;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class LoadingBriefAvailabilityTest {
    @Test void optionalProviderLessonIsOnlyPresentedWhenItsModIsLoaded() {
        var base = LoadingBriefs.FALLBACK;
        var plonk = new LoadingBrief("plonk", "controls.place_item_displays", "plonk", "", "plonk",
            "Building", "Placing Item Displays", "Press the shown key.", "Place a display with the shown key.", base.art());

        assertEquals(List.of(base), LoadingBriefs.available(List.of(base, plonk), id -> false));
        assertEquals(List.of(base, plonk), LoadingBriefs.available(List.of(base, plonk), id -> id.equals("plonk")));
    }
}
