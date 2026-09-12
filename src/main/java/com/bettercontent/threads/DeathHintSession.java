package com.bettercontent.threads;

/** A screen freezes its selection on first render, including when a late packet arrives. */
final class DeathHintSession {
    private String context = "general";
    private long receivedAt;
    private boolean frozen;
    private DeathHintRotation.Selection selection;
    private boolean recorded;

    void receive(String value, long now) {
        if (!frozen) { context = value; receivedAt = now; }
    }
    String freeze(long now) {
        frozen = true;
        return now >= receivedAt && now - receivedAt <= 5_000 ? context : "general";
    }
    DeathHintRotation.Selection selection() { return selection; }
    void select(DeathHintRotation.Selection value) { if (selection == null) selection = value; }
    boolean record() {
        if (recorded) return false;
        recorded = true;
        return true;
    }
}
