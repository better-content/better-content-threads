package com.bettercontent.threads;

import java.util.LinkedHashSet;
import java.util.List;

final class LoadingBriefSession {
    private final List<LoadingBrief> briefs;
    private final LinkedHashSet<String> viewed = new LinkedHashSet<>();
    private int index;
    static final long MINIMUM_READING_MS = 8_000L;
    private final java.util.Map<String, Long> exposure = new java.util.HashMap<>();
    private long lastObserved = -1;
    private boolean keepReading;

    LoadingBriefSession(List<LoadingBrief> briefs, LoadingBriefRotation.State state) {
        this.briefs = briefs.isEmpty() ? List.of(LoadingBriefs.FALLBACK) : List.copyOf(briefs);
        LoadingBrief selected = LoadingBriefRotation.select(this.briefs, state);
        this.index = Math.max(0, this.briefs.indexOf(selected));
    }

    // Count rendered time only; loading stalls and invisible screens do not consume a lesson.
    void observe(long now) {
        if (lastObserved >= 0) {
            long delta = Math.max(0, Math.min(250, now - lastObserved));
            long total = exposure.merge(current().id(), delta, Long::sum);
            if (total >= MINIMUM_READING_MS) viewed.add(current().id());
        }
        lastObserved = now;
    }

    LoadingBrief current() {
        return briefs.get(index);
    }

    int index() {
        return index;
    }

    int size() {
        return briefs.size();
    }

    List<String> viewed() {
        return List.copyOf(viewed);
    }

    boolean keepReading() {
        return keepReading;
    }

    boolean toggleKeepReading() {
        keepReading = !keepReading;
        return keepReading;
    }

    LoadingBrief move(int delta) {
        index = Math.floorMod(index + delta, briefs.size());
        lastObserved = -1;
        return current();
    }
}
