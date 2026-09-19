package com.bettercontent.threads;

/** Geometry for the in-world unread Threads prompt. */
record UnreadBadgeLayout(int plateX, int contentX, int contentWidth, boolean showLabel) {
    private static final int PLATE_WIDTH = 18;
    private static final int GAP = 5;
    private static final int RIGHT_MARGIN = 6;

    static UnreadBadgeLayout calculate(int screenWidth, int keyWidth, int labelWidth) {
        int availableContent = Math.max(0, screenWidth - PLATE_WIDTH - GAP - RIGHT_MARGIN);
        int contentWidth = Math.min(Math.max(keyWidth, labelWidth), availableContent);
        int plateX = Math.max(0, screenWidth - PLATE_WIDTH - GAP - contentWidth - RIGHT_MARGIN);
        return new UnreadBadgeLayout(plateX, plateX + PLATE_WIDTH + GAP, contentWidth,
            labelWidth <= contentWidth);
    }

    int right() {
        return contentX + contentWidth;
    }
}
