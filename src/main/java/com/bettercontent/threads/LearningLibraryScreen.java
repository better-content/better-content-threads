package com.bettercontent.threads;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

final class LearningLibraryScreen extends Screen {
    private static final int LIST_TOP = 48;
    private static final int ROW_HEIGHT = 34;
    private int columns() { return width < 500 ? 1 : 2; }

    private final List<ThreadNetwork.Card> cards;
    private final List<LoadingBrief> briefs;
    private final LoadingBriefSession session;
    private LoadingBriefRotation.State state;
    private Set<String> seen;
    private int scrollRow;
    private boolean detail;
    private int textScroll;
    private int textMaximumScroll;
    private Button previous;
    private Button back;
    private Button next;
    private Button related;
    private Button guide;

    LearningLibraryScreen(List<ThreadNetwork.Card> cards) {
        super(Component.translatable("screen.better_content_threads.lessons"));
        this.cards = List.copyOf(cards);
        this.briefs = LoadingBriefs.INSTANCE.all();
        this.state = LoadingBriefStore.load();
        this.seen = new HashSet<>(state.seen());
        this.session = new LoadingBriefSession(briefs, state);
        this.scrollRow = session.index() / columns();
    }

    LearningLibraryScreen(List<ThreadNetwork.Card> cards, String lessonId) {
        this(cards);
        for (int index = 0; index < briefs.size(); index++) {
            if (!briefs.get(index).id().equals(lessonId)) continue;
            session.move(index - session.index());
            detail = true;
            markViewed();
            break;
        }
    }

    @Override
    protected void init() {
        addRenderableWidget(Button.builder(Component.translatable("screen.better_content_threads.cards"),
                button -> minecraft.setScreen(new ThreadDeckScreen(cards)))
            .bounds(8, 8, 68, 20).build());
        var layout = LoadingBriefLayout.calculate(width, height, false, 2);
        previous = addRenderableWidget(Button.builder(Component.translatable("screen.better_content_threads.loading_previous"),
                button -> move(-1, true))
            .bounds(width / 2 - 146, layout.controlsY(), 88, 20).build());
        back = addRenderableWidget(Button.builder(Component.translatable("screen.better_content_threads.lessons"),
                button -> showList())
            .bounds(width / 2 - 50, layout.controlsY(), 100, 20).build());
        next = addRenderableWidget(Button.builder(Component.translatable("screen.better_content_threads.loading_next"),
                button -> move(1, true))
            .bounds(width / 2 + 58, layout.controlsY(), 88, 20).build());
        related = addRenderableWidget(Button.builder(Component.translatable("screen.better_content_threads.related_thread"),
                button -> openRelatedThread())
            .bounds(width / 2 - 104, layout.controlsY() - 24, 100, 20).build());
        guide = addRenderableWidget(Button.builder(Component.translatable("screen.better_content_threads.open_guide"),
                button -> openGuide())
            .bounds(width / 2 + 4, layout.controlsY() - 24, 100, 20).build());
        updateButtons();
    }

    @Override
    public boolean isPauseScreen() {
        return true;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        graphics.fill(0, 0, width, height, 0xEF101412);
        graphics.drawCenteredString(font, detail ? "THREADS · LESSON" : "THREADS · LESSONS", width / 2, 14, 0xFFF0E5CE);
        if (detail) renderDetail(graphics);
        else renderList(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
    }

    private void renderList(GuiGraphics graphics) {
        graphics.drawCenteredString(font, "Spoiler-free fundamentals · all lessons available", width / 2, 34, 0xFFBAB8AB);
        int cellWidth = Math.min(560, Math.max(1, (width - 24) / columns()));
        int startX = (width - cellWidth * columns()) / 2;
        int visibleRows = visibleRows();
        graphics.drawCenteredString(font, "↑ ↓ Select · Enter Read · Scroll for more", width / 2, height - 10, 0xFFBAB8AB);
        scrollRow = Math.max(0, Math.min(scrollRow, maximumScroll(visibleRows)));
        for (int index = 0; index < briefs.size(); index++) {
            int row = index / columns() - scrollRow;
            if (row < 0 || row >= visibleRows) continue;
            int column = index % columns();
            int x = startX + column * cellWidth;
            int y = LIST_TOP + row * ROW_HEIGHT;
            var brief = briefs.get(index);
            boolean selected = index == session.index();
            if (selected) graphics.fill(x, y, x + cellWidth - 2, y + ROW_HEIGHT - 2, 0x38C6A15B);
            graphics.fill(x, y, x + 2, y + ROW_HEIGHT - 2, selected ? 0xFFC6A15B : 0x667A735F);
            graphics.drawString(font, fit(brief.headline(), cellWidth - 12), x + 7, y + 6, 0xFFF0E5CE, false);
            String stateLabel = seen.contains(brief.id())
                ? Component.translatable("screen.better_content_threads.lesson_seen").getString()
                : brief.category().toUpperCase();
            graphics.drawString(font, fit(stateLabel, cellWidth - 12), x + 7, y + 19,
                seen.contains(brief.id()) ? 0xFF9BB59B : 0xFF9FA996, false);
        }
    }

    private void renderDetail(GuiGraphics graphics) {
        var layout = LoadingBriefLayout.calculate(width, height, false, 2);
        var text = ThreadClient.lessonText(session, layout.textWidth() - 8);
        // Prefer the complete lesson at normal font size over a decorative side image.
        if (text.height() > layout.panelHeight() - 24 && layout.showArt()) {
            layout = LoadingBriefLayout.calculate(width, height, false, 2, false);
            text = ThreadClient.lessonText(session, layout.textWidth() - 8);
        }
        textMaximumScroll = text.maximumScroll(layout.panelHeight() - 24);
        textScroll = Math.max(0, Math.min(textScroll, textMaximumScroll));
        ThreadClient.renderLessonCard(graphics, session, layout, textScroll);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (super.mouseClicked(mouseX, mouseY, button)) return true;
        if (detail) return true;
        int cellWidth = Math.min(560, Math.max(1, (width - 24) / columns()));
        int startX = (width - cellWidth * columns()) / 2;
        int visibleRows = visibleRows();
        for (int index = 0; index < briefs.size(); index++) {
            int row = index / columns() - scrollRow;
            int column = index % columns();
            int x = startX + column * cellWidth;
            int y = LIST_TOP + row * ROW_HEIGHT;
            if (row >= 0 && row < visibleRows && mouseX >= x && mouseX < x + cellWidth
                    && mouseY >= y && mouseY < y + ROW_HEIGHT) {
                select(index);
                showDetail();
                return true;
            }
        }
        return true;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if ((keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_SPACE) && getFocused() instanceof Button)
            return super.keyPressed(keyCode, scanCode, modifiers);
        if (detail) {
            if (keyCode == GLFW.GLFW_KEY_UP || keyCode == GLFW.GLFW_KEY_DOWN || keyCode == GLFW.GLFW_KEY_PAGE_UP || keyCode == GLFW.GLFW_KEY_PAGE_DOWN) {
                int amount = (keyCode == GLFW.GLFW_KEY_PAGE_UP || keyCode == GLFW.GLFW_KEY_PAGE_DOWN) ? 72 : 12;
                textScroll = Math.max(0, Math.min(textMaximumScroll, textScroll + ((keyCode == GLFW.GLFW_KEY_UP || keyCode == GLFW.GLFW_KEY_PAGE_UP) ? -amount : amount)));
                return true;
            }
            if (keyCode == GLFW.GLFW_KEY_LEFT) { move(-1, true); return true; }
            if (keyCode == GLFW.GLFW_KEY_RIGHT) { move(1, true); return true; }
            if (keyCode == GLFW.GLFW_KEY_ESCAPE) { showList(); return true; }
        } else {
            if (keyCode == GLFW.GLFW_KEY_LEFT) { move(-1, false); return true; }
            if (keyCode == GLFW.GLFW_KEY_RIGHT) { move(1, false); return true; }
            if (keyCode == GLFW.GLFW_KEY_UP) { move(-columns(), false); return true; }
            if (keyCode == GLFW.GLFW_KEY_DOWN) { move(columns(), false); return true; }
            if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER || keyCode == GLFW.GLFW_KEY_SPACE) {
                showDetail();
                return true;
            }
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if (detail) textScroll = Math.max(0, Math.min(textMaximumScroll, textScroll + (delta < 0 ? 24 : -24)));
        else move(delta < 0 ? columns() : -columns(), false);
        return true;
    }

    private void select(int index) {
        session.move(index - session.index());
        ensureVisible();
    }

    private void move(int delta, boolean mark) {
        session.move(delta);
        textScroll = 0;
        ensureVisible();
        if (mark) markViewed();
        updateButtons();
    }

    private void showDetail() {
        detail = true;
        textScroll = 0;
        markViewed();
        updateButtons();
    }

    private void showList() {
        detail = false;
        updateButtons();
    }

    private void markViewed() {
        state = LoadingBriefRotation.commit(state, List.of(session.current().id()), briefs);
        LoadingBriefStore.save(state);
        seen = new HashSet<>(state.seen());
    }

    private ThreadNetwork.Card relatedCard() {
        String id = session.current().relatedThread();
        if (id.isEmpty()) return null;
        return cards.stream().filter(card -> card.id().equals(id)).findFirst().orElse(null);
    }

    private void openRelatedThread() {
        var card = relatedCard();
        if (card != null && card.known()) Minecraft.getInstance().setScreen(new ThreadDeckScreen(cards, card.id()));
    }

    private void openGuide() {
        var card = relatedCard();
        if (card != null && card.known() && ThreadDoorways.available(card)) ThreadDoorways.open(card);
    }

    private void updateButtons() {
        if (previous == null) return;
        previous.visible = detail;
        back.visible = detail;
        next.visible = detail;
        var card = relatedCard();
        related.visible = detail && card != null && card.known();
        guide.visible = detail && card != null && card.known() && ThreadDoorways.available(card);
        if (card != null) guide.setMessage(ThreadDoorways.label(card));
        if (related.visible && !guide.visible) related.setX(width / 2 - 50);
        else related.setX(width / 2 - 104);
        if (guide.visible && !related.visible) guide.setX(width / 2 - 50);
        else guide.setX(width / 2 + 4);
    }

    private void ensureVisible() {
        int row = session.index() / columns();
        int visible = visibleRows();
        if (row < scrollRow) scrollRow = row;
        if (row >= scrollRow + visible) scrollRow = row - visible + 1;
    }

    private int visibleRows() {
        return Math.max(1, (height - LIST_TOP - 18) / ROW_HEIGHT);
    }

    private int maximumScroll(int visibleRows) {
        return Math.max(0, (briefs.size() + columns() - 1) / columns() - visibleRows);
    }

    private String fit(String text, int maxWidth) {
        if (font.width(text) <= maxWidth) return text;
        String value = text;
        while (value.length() > 1 && font.width(value + "…") > maxWidth) value = value.substring(0, value.length() - 1);
        return value + "…";
    }
}
