package com.bettercontent.threads;

record LoadingBriefLayout(
        int headerY,
        int barX,
        int barY,
        int barWidth,
        int panelX,
        int panelY,
        int panelWidth,
        int panelHeight,
        int artX,
        int artY,
        int artWidth,
        int artHeight,
        int textX,
        int textY,
        int textWidth,
        int controlsY,
        boolean showArt
) {
    static LoadingBriefLayout calculate(int screenWidth, int screenHeight, boolean progress) {
        int margin = 12;
        int headerY = 12;
        int barWidth = Math.min(560, Math.max(80, screenWidth - 40));
        int barX = (screenWidth - barWidth) / 2;
        int barY = progress ? 34 : 0;
        int controlsY = Math.max(progress ? 62 : 48, screenHeight - 28);
        int panelY = progress ? 54 : 38;
        int availableHeight = Math.max(80, controlsY - panelY - 8);
        int panelHeight = Math.min(260, availableHeight);
        int panelWidth = Math.min(700, Math.max(120, screenWidth - margin * 2));
        int panelX = (screenWidth - panelWidth) / 2;
        boolean showArt = panelWidth >= 356 && panelHeight >= 112;
        int artWidth = showArt ? 2 * (Math.min(280, Math.max(112, panelWidth * 2 / 5 - 12)) / 2) : 0;
        int artHeight = artWidth / 2;
        int artX = panelX + 12;
        int artY = panelY + Math.max(12, (panelHeight - artHeight) / 2);
        int textX = showArt ? artX + artWidth + 14 : panelX + 14;
        int textWidth = Math.max(80, panelX + panelWidth - 14 - textX);
        return new LoadingBriefLayout(headerY, barX, barY, barWidth, panelX, panelY, panelWidth,
            panelHeight, artX, artY, artWidth, artHeight, textX, panelY + 12, textWidth, controlsY, showArt);
    }
}
