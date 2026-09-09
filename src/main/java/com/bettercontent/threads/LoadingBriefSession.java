package com.bettercontent.threads;

import java.util.LinkedHashSet;
import java.util.List;

final class LoadingBriefSession {
    private final List<LoadingBrief> briefs;
    private final LinkedHashSet<String> viewed = new LinkedHashSet<>();
    private int index;
    private boolean keepReading;

    LoadingBriefSession(List<LoadingBrief> briefs, LoadingBriefRotation.State state) {
        this.briefs = briefs.isEmpty() ? List.of(LoadingBriefs.FALLBACK) : List.copyOf(briefs);
        LoadingBrief selected = LoadingBriefRotation.select(this.briefs, state);
        this.index = Math.max(0, this.briefs.indexOf(selected));
        viewed.add(current().id());
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
        viewed.add(current().id());
        return current();
    }
}
