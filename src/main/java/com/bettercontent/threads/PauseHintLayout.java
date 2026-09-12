package com.bettercontent.threads;

/** Preserve button sizes; lift the group only enough to reserve the caption. */
record PauseHintLayout(int shift, int titleY, boolean fits) {
    static PauseHintLayout calculate(int height, int firstButton, int bottom, int lines, int lineHeight) {
        int captionHeight = 14 + (lines + 1) * lineHeight;
        int needed = Math.max(0, bottom + 6 + captionHeight + 8 - height);
        int shift = Math.min(needed, Math.max(0, firstButton - 36));
        return new PauseHintLayout(shift, shift > 0 ? 12 : -1, shift == needed);
    }
}
