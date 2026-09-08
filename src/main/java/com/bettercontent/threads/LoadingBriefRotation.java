package com.bettercontent.threads;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

final class LoadingBriefRotation {
    record State(Set<String> seen, String last) {
        State {
            seen = Set.copyOf(seen);
            last = last == null ? "" : last;
        }
    }

    private LoadingBriefRotation() {}

    static LoadingBrief select(List<LoadingBrief> briefs, State state) {
        if (briefs.isEmpty()) return LoadingBriefs.FALLBACK;
        if (!state.seen().contains(briefs.get(0).id())) return briefs.get(0);
        var eligible = new ArrayList<LoadingBrief>();
        for (var brief : briefs) if (!state.seen().contains(brief.id())) eligible.add(brief);
        if (eligible.isEmpty()) {
            for (var brief : briefs) if (!brief.id().equals(state.last())) eligible.add(brief);
            if (eligible.isEmpty()) eligible.add(briefs.get(0));
        }
        int seed = state.seen().hashCode() * 31 + state.last().hashCode();
        return eligible.get(Math.floorMod(seed, eligible.size()));
    }

    static State commit(State state, String id, int catalogueSize) {
        var seen = new LinkedHashSet<>(state.seen());
        if (seen.size() >= catalogueSize) seen.clear();
        seen.add(id);
        return new State(seen, id);
    }
}
