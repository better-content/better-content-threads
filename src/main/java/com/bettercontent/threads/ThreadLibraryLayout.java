package com.bettercontent.threads;

/** Stable geometry rules for the pack-owned lesson library. */
record ThreadLibraryLayout(int columns, int cellWidth, int visibleRows, int maximumScroll) {
    static ThreadLibraryLayout calculate(int width, int height, int itemCount, int scrollRow) {
        int columns = width < 500 ? 1 : 2;
        int cellWidth = Math.min(560, Math.max(1, (width - 24) / columns));
        int visibleRows = Math.max(1, (height - 48 - 18) / 34);
        int rows = Math.max(1, (itemCount + columns - 1) / columns);
        return new ThreadLibraryLayout(columns, cellWidth, visibleRows, Math.max(0, rows - visibleRows));
    }
}
