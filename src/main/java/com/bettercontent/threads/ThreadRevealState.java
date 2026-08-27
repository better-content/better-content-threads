package com.bettercontent.threads;

final class ThreadRevealState {
    static final long DURATION_MS = 1_800L;

    enum Phase { SEALED, DEVELOPING, COMPLETE }
    enum Activation { STARTED, COMPLETED, IGNORED }

    private Phase phase = Phase.COMPLETE;
    private long elapsedMs;

    void select(boolean unread) {
        phase = unread ? Phase.SEALED : Phase.COMPLETE;
        elapsedMs = 0L;
    }

    Activation activate() {
        if (phase == Phase.SEALED) {
            phase = Phase.DEVELOPING;
            return Activation.STARTED;
        }
        if (phase == Phase.DEVELOPING) {
            complete();
            return Activation.COMPLETED;
        }
        return Activation.IGNORED;
    }

    boolean advance(long deltaMs) {
        if (phase != Phase.DEVELOPING) return false;
        elapsedMs = Math.min(DURATION_MS, elapsedMs + Math.max(0L, deltaMs));
        if (elapsedMs < DURATION_MS) return false;
        complete();
        return true;
    }

    void complete() {
        phase = Phase.COMPLETE;
        elapsedMs = DURATION_MS;
    }

    Phase phase() {
        return phase;
    }

    long elapsedMs() {
        return elapsedMs;
    }
}
