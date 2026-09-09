package com.bettercontent.threads;

import java.util.Collection;
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
        for (var brief : briefs) {
            if (!state.seen().contains(brief.id())) return brief;
        }
        for (int index = 0; index < briefs.size(); index++) {
            if (briefs.get(index).id().equals(state.last())) return briefs.get((index + 1) % briefs.size());
        }
        return briefs.get(0);
    }

    static State commit(State state, Collection<String> viewed, List<LoadingBrief> briefs) {
        var valid = new LinkedHashSet<String>();
        briefs.forEach(brief -> valid.add(brief.id()));
        var seen = new LinkedHashSet<String>();
        state.seen().stream().filter(valid::contains).forEach(seen::add);
        String last = valid.contains(state.last()) ? state.last() : "";
        for (String id : viewed) {
            if (!valid.contains(id)) continue;
            if (seen.size() >= valid.size()) seen.clear();
            seen.add(id);
            last = id;
        }
        return new State(seen, last);
    }
}
