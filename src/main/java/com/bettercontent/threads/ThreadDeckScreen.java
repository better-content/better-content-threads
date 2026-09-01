package com.bettercontent.threads;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class ThreadDeckScreen extends Screen {
    private static final int CATALOGUE_TOP = 62;
    private static final int CATALOGUE_ROW_HEIGHT = 34;
    private static final int CATALOGUE_COLUMNS = 2;
    private static final int DETAIL_MARGIN = 12;
    private static final int DETAIL_GAP = 18;
    private static final int DETAIL_MIN_PANEL_WIDTH = 96;
    private static final int DETAIL_MAX_PANEL_WIDTH = 230;

    private final List<ThreadNetwork.Card> cards;
    private final Set<String> readHere = new HashSet<>();
    private final ThreadRevealState reveal = new ThreadRevealState();
    private ThreadSuit suit = ThreadSuit.WORLD;
    private int selected;
    private int scrollRow;
    private boolean detail;
    private long lastFrame;

    ThreadDeckScreen(List<ThreadNetwork.Card> cards) {
        super(Component.literal("Threads"));
        this.cards = new ArrayList<>(cards);
        for (var card : cards) {
            if (card.known() && card.unread()) {
                suit = ThreadSuit.parse(card.suit());
                selected = card.order() - 1;
                scrollRow = selected / CATALOGUE_COLUMNS;
                break;
            }
        }
        selectCurrent();
    }

    @Override
    public boolean isPauseScreen() {
        return true;
    }

    private List<ThreadNetwork.Card> suitCards() {
        return suitCards(suit);
    }

    private List<ThreadNetwork.Card> suitCards(ThreadSuit candidate) {
        return cards.stream()
            .filter(card -> card.suit().equals(candidate.id()))
            .sorted(Comparator.comparingInt(ThreadNetwork.Card::order))
            .toList();
    }

    private ThreadNetwork.Card current() {
        var list = suitCards();
        return list.isEmpty() ? null : list.get(Math.floorMod(selected, list.size()));
    }

    private boolean unread(ThreadNetwork.Card card) {
        return card.known() && card.unread() && !readHere.contains(card.id());
    }

    private int unreadCount(ThreadSuit candidate) {
        return (int) suitCards(candidate).stream().filter(this::unread).count();
    }

    private int totalUnread() {
        return (int) cards.stream().filter(this::unread).count();
    }

    private int firstUnreadIndex(List<ThreadNetwork.Card> list) {
        for (int index = 0; index < list.size(); index++) {
            if (unread(list.get(index))) return index;
        }
        return 0;
    }

    private void selectSuit(ThreadSuit candidate) {
        suit = candidate;
        selected = firstUnreadIndex(suitCards());
        scrollRow = selected / CATALOGUE_COLUMNS;
        detail = false;
        selectCurrent();
    }

    private void selectCurrent() {
        var card = current();
        reveal.select(card != null && unread(card));
        lastFrame = System.currentTimeMillis();
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partial) {
        long now = System.currentTimeMillis();
        long delta = Math.min(100L, Math.max(0L, now - lastFrame));
        lastFrame = now;
        var card = current();
        if (detail && card != null && reveal.advance(delta)) finishDevelopment(card);
        renderBackground(graphics);
        graphics.drawCenteredString(font, "THREADS", width / 2, 10, 0xFFF0E5CE);
        renderTabs(graphics);
        if (detail) renderDetail(graphics, card);
        else renderCatalogue(graphics);
        super.render(graphics, mouseX, mouseY, partial);
    }

    private void renderTabs(GuiGraphics graphics) {
        int total = Math.min(width - 20, 320);
        int tabWidth = total / 4;
        int start = (width - total) / 2;
        for (var candidate : ThreadSuit.values()) {
            int x = start + candidate.ordinal() * tabWidth;
            boolean active = candidate == suit;
            graphics.fill(x, 24, x + tabWidth - 2, 43, ((active ? 0xD0 : 0x66) << 24) | candidate.color());
            int count = unreadCount(candidate);
            int labelRight = x + tabWidth - 2;
            if (count > 0) {
                String value = Integer.toString(count);
                int badgeWidth = Math.max(10, font.width(value) + 4);
                int badgeX = x + tabWidth - badgeWidth - 5;
                graphics.fill(badgeX, 28, badgeX + badgeWidth, 39, 0xE0C6A15B);
                graphics.drawCenteredString(font, value, badgeX + badgeWidth / 2, 30, 0xFF111513);
                labelRight = badgeX - 2;
            }
            graphics.drawCenteredString(font, capital(candidate.id()), x + (labelRight - x) / 2, 30, 0xFFFFFFFF);
        }
    }

    private void renderCatalogue(GuiGraphics graphics) {
        var list = suitCards();
        int remembered = (int) list.stream().filter(ThreadNetwork.Card::known).count();
        int unread = totalUnread();
        Component status = unread > 0
            ? Component.translatable("screen.better_content_threads.catalogue_status_unread", remembered, unread)
            : Component.translatable("screen.better_content_threads.catalogue_status", remembered);
        graphics.drawCenteredString(font, status, width / 2, 48, 0xFF928B80);

        int cellWidth = Math.min(250, (width - 24) / CATALOGUE_COLUMNS);
        int startX = (width - cellWidth * CATALOGUE_COLUMNS) / 2;
        int visibleRows = Math.max(1, (height - CATALOGUE_TOP - 14) / CATALOGUE_ROW_HEIGHT);
        scrollRow = Math.max(0, Math.min(scrollRow, Math.max(0, 7 - visibleRows)));
        Component revealLabel = Component.translatable("screen.better_content_threads.reveal_badge");
        int revealWidth = font.width(revealLabel) + 6;

        for (int index = 0; index < list.size(); index++) {
            int row = index / CATALOGUE_COLUMNS - scrollRow;
            if (row < 0 || row >= visibleRows) continue;
            int column = index % CATALOGUE_COLUMNS;
            int x = startX + column * cellWidth;
            int y = CATALOGUE_TOP + row * CATALOGUE_ROW_HEIGHT;
            var card = list.get(index);
            boolean needsReveal = unread(card);
            if (needsReveal) {
                graphics.fill(x, y, x + cellWidth - 2, y + CATALOGUE_ROW_HEIGHT - 2, 0x28C6A15B);
                graphics.fill(x, y, x + 2, y + CATALOGUE_ROW_HEIGHT - 2, 0xFFC6A15B);
            }
            renderThumb(graphics, card, x + 3, y + 3, 16, 24, needsReveal);
            int color = card.known() ? 0xFFF0E5CE : 0xFF9A948B;
            int titleWidth = cellWidth - 29 - (needsReveal ? revealWidth + 8 : 0);
            graphics.drawString(font, fit(card.title(), titleWidth), x + 26, y + 7, color, false);
            graphics.drawString(font, capital(card.aspect()), x + 26, y + 19,
                ThreadAspect.parse(card.aspect()).color() | 0xFF000000, false);
            if (needsReveal) {
                int badgeX = x + cellWidth - revealWidth - 4;
                graphics.fill(badgeX, y + 5, badgeX + revealWidth, y + 16, 0xE0C6A15B);
                graphics.drawCenteredString(font, revealLabel, badgeX + revealWidth / 2, y + 7, 0xFF111513);
            }
        }
    }

    private void renderThumb(GuiGraphics graphics, ThreadNetwork.Card card, int x, int y, int cardWidth, int cardHeight,
                             boolean selected) {
        if (!card.known() || unread(card)) {
            ThreadClient.renderSealedPlate(graphics, x, y, cardWidth, cardHeight,
                ThreadSuit.parse(card.suit()).color(), ThreadAspect.parse(card.aspect()).color(),
                card.id().hashCode(), selected);
            return;
        }
        graphics.fill(x - 2, y - 2, x + cardWidth + 2, y + cardHeight + 2,
            ((selected ? 0xCC : 0x66) << 24) | ThreadSuit.parse(card.suit()).color());
        ThreadClient.renderArt(graphics, ThreadClient.layer(card.art(), "thumb"), x, y, cardWidth, cardHeight);
    }

    private void renderDetail(GuiGraphics graphics, ThreadNetwork.Card card) {
        if (card == null) return;
        var layout = detailLayout(width, height);
        graphics.drawString(font, "‹ Catalogue", 12, 48, 0xFFB6A98D, false);
        graphics.fill(layout.cardX() - 4, layout.cardY() - 4,
            layout.cardX() + layout.cardWidth() + 4, layout.cardY() + layout.cardHeight() + 4, 0xFF252421);
        graphics.fill(layout.cardX() - 2, layout.cardY() - 2,
            layout.cardX() + layout.cardWidth() + 2, layout.cardY() + layout.cardHeight() + 2,
            0xFF000000 | ThreadSuit.parse(card.suit()).color());
        if (!card.known()) {
            ThreadClient.renderSealedPlate(graphics, layout.cardX(), layout.cardY(), layout.cardWidth(), layout.cardHeight(),
                ThreadSuit.parse(card.suit()).color(), ThreadAspect.parse(card.aspect()).color(),
                card.id().hashCode(), true);
            renderLocked(graphics, card, layout.detailsX(), layout.cardY());
            return;
        }
        renderCard(graphics, card, layout.cardX(), layout.cardY(), layout.cardWidth(), layout.cardHeight());
        renderDetails(graphics, card, layout.detailsX(), layout.cardY(), layout.panelWidth(), layout.cardHeight());
    }

    private void renderLocked(GuiGraphics graphics, ThreadNetwork.Card card, int x, int y) {
        graphics.drawString(font, card.title(), x, y + 4, 0xFFF0E5CE, false);
        graphics.drawString(font, capital(card.suit()) + " · " + capital(card.aspect()), x, y + 19, 0xFF9A948B, false);
        graphics.drawString(font, card.future()
            ? "This plate belongs to a system beyond this world."
            : "The plate has not answered yet.", x, y + 48, 0xFF756F68, false);
    }

    private void renderCard(GuiGraphics graphics, ThreadNetwork.Card card, int x, int y, int cardWidth, int cardHeight) {
        if (reveal.phase() == ThreadRevealState.Phase.COMPLETE) {
            ThreadClient.renderArt(graphics, card.art(), x, y, cardWidth, cardHeight);
            return;
        }
        ThreadClient.renderSealedPlate(graphics, x, y, cardWidth, cardHeight,
            ThreadSuit.parse(card.suit()).color(), ThreadClient.ARCHIVE_GOLD, card.id().hashCode(), true);
        if (reveal.phase() == ThreadRevealState.Phase.DEVELOPING) {
            float progress = Math.min(1.0f, reveal.elapsedMs() / (float) ThreadRevealState.DURATION_MS);
            ThreadClient.renderArt(graphics, card.art(), x, y, cardWidth, cardHeight, progress);
        }
    }

    private void renderDetails(GuiGraphics graphics, ThreadNetwork.Card card, int x, int y, int panelWidth, int cardHeight) {
        if (reveal.phase() == ThreadRevealState.Phase.SEALED) {
            graphics.drawString(font, "Let the plate develop", x, y + Math.min(80, cardHeight / 2), 0xFFA99573, false);
            graphics.drawString(font, "Click or Space to remember", x,
                y + Math.min(94, cardHeight / 2 + 14), 0xFF7F796F, false);
            return;
        }
        if (reveal.phase() != ThreadRevealState.Phase.COMPLETE) return;
        graphics.drawString(font, fit(card.title(), panelWidth), x, y + 2, 0xFFF0E5CE, false);
        graphics.drawString(font, capital(card.suit()) + " · " + capital(card.aspect()), x, y + 14, 0xFF9A948B, false);
        int lineY = y + 28;
        for (var line : font.split(Component.literal(card.prose()), panelWidth)) {
            if (lineY > y + 70) break;
            graphics.drawString(font, line, x, lineY, 0xFFC8C0B0, false);
            lineY += 11;
        }
        if (card.active()) {
            graphics.drawString(font, fit(card.invitation(), panelWidth), x, y + 76, 0xFFA99573, false);
            for (var line : font.split(Component.literal(card.action()), panelWidth)) {
                graphics.drawString(font, line, x, y + 88, 0xFFF0E5CE, false);
                break;
            }
        } else {
            graphics.drawString(font, "Remembered in an earlier world", x, y + 76, 0xFF7F796F, false);
        }
        if (card.completed()) graphics.drawString(font, "Completed in this world", x, y + 101, 0xFF9BB59B, false);
        if (card.completionCount() > 0) {
            graphics.drawString(font, "Remembered " + card.completionCount() + " time"
                + (card.completionCount() == 1 ? "" : "s"), x, y + 112, 0xFF928B80, false);
        }
        if (cardHeight >= 175 && !card.routeSummary().isEmpty()) {
            graphics.drawString(font, fit(card.routeSummary(), panelWidth), x, y + 124, 0xFF77736B, false);
        }
        if (!card.doorwayType().isEmpty()) graphics.drawString(font, "Look closer  ›", x, y + cardHeight - 27, 0xFFAEBFD0, false);
        graphics.drawString(font, "Issue signed facsimile", x, y + cardHeight - 14, 0xFFC9AE7A, false);
    }

    private String fit(String text, int maxWidth) {
        maxWidth = Math.max(1, maxWidth);
        if (font.width(text) <= maxWidth) return text;
        String value = text;
        while (value.length() > 1 && font.width(value + "…") > maxWidth) value = value.substring(0, value.length() - 1);
        return value + "…";
    }

    private static String capital(String value) {
        return Character.toUpperCase(value.charAt(0)) + value.substring(1);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int total = Math.min(width - 20, 320);
        int tabWidth = total / 4;
        int start = (width - total) / 2;
        if (mouseY >= 24 && mouseY < 43 && mouseX >= start && mouseX < start + total) {
            selectSuit(ThreadSuit.values()[Math.min(3, (int) ((mouseX - start) / tabWidth))]);
            return true;
        }
        if (!detail) {
            var list = suitCards();
            int cellWidth = Math.min(250, (width - 24) / CATALOGUE_COLUMNS);
            int startX = (width - cellWidth * CATALOGUE_COLUMNS) / 2;
            int visibleRows = Math.max(1, (height - CATALOGUE_TOP - 14) / CATALOGUE_ROW_HEIGHT);
            for (int index = 0; index < list.size(); index++) {
                int row = index / CATALOGUE_COLUMNS - scrollRow;
                int column = index % CATALOGUE_COLUMNS;
                int x = startX + column * cellWidth;
                int y = CATALOGUE_TOP + row * CATALOGUE_ROW_HEIGHT;
                if (row >= 0 && row < visibleRows && mouseX >= x && mouseX < x + cellWidth
                    && mouseY >= y && mouseY < y + CATALOGUE_ROW_HEIGHT) {
                    selected = index;
                    detail = true;
                    selectCurrent();
                    return true;
                }
            }
            return true;
        }
        var card = current();
        if (card == null) return true;
        if (mouseX < 100 && mouseY >= 43 && mouseY < 60) {
            detail = false;
            return true;
        }
        if (unread(card)) {
            activateReveal(card);
            return true;
        }
        if (!card.known()) return true;
        var layout = detailLayout(width, height);
        if (!card.doorwayType().isEmpty() && mouseX >= layout.detailsX() && mouseY >= layout.cardY() + layout.cardHeight() - 32
            && mouseY < layout.cardY() + layout.cardHeight() - 18) {
            ThreadDoorways.open(card);
            return true;
        }
        if (mouseX >= layout.detailsX() && mouseY >= layout.cardY() + layout.cardHeight() - 18
            && mouseY < layout.cardY() + layout.cardHeight()) {
            ThreadNetwork.request("issue", card.id());
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private void activateReveal(ThreadNetwork.Card card) {
        if (reveal.activate() == ThreadRevealState.Activation.COMPLETED) finishDevelopment(card);
    }

    private void finishDevelopment(ThreadNetwork.Card card) {
        reveal.complete();
        if (readHere.add(card.id())) ThreadNetwork.request("read", card.id());
    }

    @Override
    public boolean keyPressed(int key, int scan, int mods) {
        var card = current();
        if (!detail && (key == GLFW.GLFW_KEY_ENTER || key == GLFW.GLFW_KEY_SPACE)) {
            detail = true;
            selectCurrent();
            return true;
        }
        if (detail && card != null && key == GLFW.GLFW_KEY_SPACE && unread(card)) {
            activateReveal(card);
            return true;
        }
        if (detail && (key == GLFW.GLFW_KEY_LEFT || key == GLFW.GLFW_KEY_RIGHT)) {
            selected = Math.floorMod(selected + (key == GLFW.GLFW_KEY_RIGHT ? 1 : -1), 13);
            selectCurrent();
            return true;
        }
        if (key == GLFW.GLFW_KEY_ESCAPE && detail) {
            detail = false;
            return true;
        }
        return super.keyPressed(key, scan, mods);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if (detail) {
            selected = Math.floorMod(selected + (delta < 0 ? 1 : -1), 13);
            selectCurrent();
            return true;
        }
        int visibleRows = Math.max(1, (height - CATALOGUE_TOP - 14) / CATALOGUE_ROW_HEIGHT);
        scrollRow = Math.max(0, Math.min(Math.max(0, 7 - visibleRows), scrollRow + (delta < 0 ? 1 : -1)));
        return true;
    }

    static DetailLayout detailLayout(int screenWidth, int screenHeight) {
        int availableWidth = Math.max(1, screenWidth - DETAIL_MARGIN * 2);
        int availableHeight = Math.max(1, screenHeight - 92);
        int maximumCardWidth = Math.max(1, availableWidth - DETAIL_GAP - DETAIL_MIN_PANEL_WIDTH);
        int maximumCardHeightFromWidth = Math.max(1, maximumCardWidth * 3 / 2);
        int cardHeight = Math.max(1, Math.min(300, Math.min(availableHeight, maximumCardHeightFromWidth)));
        int cardWidth = Math.max(1, cardHeight * 2 / 3);
        int panelWidth = Math.max(1, Math.min(DETAIL_MAX_PANEL_WIDTH, availableWidth - cardWidth - DETAIL_GAP));
        int contentWidth = cardWidth + DETAIL_GAP + panelWidth;
        int cardX = Math.max(0, (screenWidth - contentWidth) / 2);
        return new DetailLayout(cardX, 56, cardWidth, cardHeight, cardX + cardWidth + DETAIL_GAP, panelWidth);
    }

    record DetailLayout(int cardX, int cardY, int cardWidth, int cardHeight, int detailsX, int panelWidth) {}
}
