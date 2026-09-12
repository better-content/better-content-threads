package com.bettercontent.threads;

record DeathHintLayout(int x, int y, int width, int height, boolean visible) {
    static int textWidth(int screenWidth) { return Math.max(1, Math.min(420, screenWidth - 40)); }
    static DeathHintLayout calculate(int width, int height, int controlsBottom, int lines, int lineHeight) {
        int textWidth = textWidth(width);
        int panelHeight = 6 + lineHeight + 3 + lines * lineHeight + 5;
        int top = controlsBottom + 6;
        return new DeathHintLayout((width - textWidth) / 2 - 8, top, textWidth + 16, panelHeight,
            width >= 160 && top + panelHeight <= height - 8);
    }
}
