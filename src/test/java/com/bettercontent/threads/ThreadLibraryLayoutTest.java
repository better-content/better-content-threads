package com.bettercontent.threads;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
final class ThreadLibraryLayoutTest {
    @Test void narrowViewportUsesOneColumnAndClampsRows() {
        var layout = ThreadLibraryLayout.calculate(240, 120, 20, 99);
        assertEquals(1, layout.columns());
        assertEquals(1, layout.visibleRows());
        assertEquals(19, layout.maximumScroll());
    }
    @Test void wideViewportUsesTwoColumnsAndEmptyStateHasScrollableSafeGeometry() {
        var layout = ThreadLibraryLayout.calculate(900, 700, 0, 0);
        assertEquals(2, layout.columns());
        assertTrue(layout.cellWidth() > 1);
        assertEquals(0, layout.maximumScroll());
    }
}
