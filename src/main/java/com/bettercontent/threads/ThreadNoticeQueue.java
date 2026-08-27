package com.bettercontent.threads;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Deque;
import java.util.function.Function;

final class ThreadNoticeQueue<T> {
    static final long DURATION_MS = 3_200L;

    record Frame<T>(T notice, long elapsedMs, boolean started) {}

    private final Deque<T> queue = new ArrayDeque<>();
    private final Function<T, String> identity;
    private long elapsedMs;
    private boolean announced;

    ThreadNoticeQueue(Function<T, String> identity) {
        this.identity = identity;
    }

    void addAll(Collection<T> additions) {
        for (T addition : additions) {
            String id = identity.apply(addition);
            if (queue.stream().noneMatch(existing -> identity.apply(existing).equals(id))) queue.addLast(addition);
        }
    }

    Frame<T> advance(long deltaMs, boolean paused) {
        if (queue.isEmpty()) return null;
        if (paused) return new Frame<>(queue.getFirst(), elapsedMs, false);
        boolean started = !announced;
        announced = true;
        elapsedMs = Math.min(DURATION_MS, elapsedMs + Math.max(0L, deltaMs));
        if (elapsedMs >= DURATION_MS) {
            queue.removeFirst();
            elapsedMs = 0L;
            announced = false;
            return null;
        }
        return new Frame<>(queue.getFirst(), elapsedMs, started);
    }

    int size() {
        return queue.size();
    }
}
