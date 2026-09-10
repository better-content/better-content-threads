package com.bettercontent.threads;

record LoadingBriefBackdropLayout(
        int headerY,
        int barX,
        int barY,
        int barWidth,
        int artX,
        int artY,
        int artWidth,
        int artHeight,
        int captionX,
        int captionY,
        int captionWidth,
        int captionHeight,
        int textX,
        int textY,
        int textWidth,
        int controlsY
) {
    static LoadingBriefBackdropLayout calculate(int screenWidth, int screenHeight, boolean progress) {
        return calculate(screenWidth, screenHeight, progress, 1);
    }

    static LoadingBriefBackdropLayout calculate(int screenWidth, int screenHeight, boolean progress,
                                                  int controlRows) {
        int headerY = 12;
        int barWidth = Math.min(560, Math.max(80, screenWidth - 40));
        int barX = (screenWidth - barWidth) / 2;
        int barY = progress ? 34 : 0;
        int controlsY = Math.max(progress ? 62 : 48, screenHeight - 28);

        int artWidth = Math.min(screenWidth, screenHeight * 2);
        artWidth -= Math.floorMod(artWidth, 2);
        artWidth = Math.max(2, artWidth);
        int artHeight = artWidth / 2;
        int artX = (screenWidth - artWidth) / 2;
        int artY = (screenHeight - artHeight) / 2;

        int captionWidth = Math.min(700, Math.max(120, screenWidth - 24));
        int captionX = (screenWidth - captionWidth) / 2;
        int reservedControls = Math.max(0, controlRows - 1) * 24;
        int captionBottom = controlsY - 8 - reservedControls;
        int captionTopLimit = progress ? barY + 28 : 26;
        int desiredCaptionHeight = Math.min(124, Math.max(100, screenHeight / 4));
        int captionHeight = Math.max(64, Math.min(desiredCaptionHeight, captionBottom - captionTopLimit));
        int captionY = captionBottom - captionHeight;
        int textX = captionX + 12;
        int textY = captionY + 8;
        int textWidth = Math.max(80, captionWidth - 24);
        return new LoadingBriefBackdropLayout(headerY, barX, barY, barWidth, artX, artY, artWidth,
            artHeight, captionX, captionY, captionWidth, captionHeight, textX, textY, textWidth, controlsY);
    }
}
