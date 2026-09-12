package com.bettercontent.threads;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.random.RandomGenerator;

/** Both surfaces draw from one cycle; selection alone never consumes an entry. */
final class DeathHintRotation {
    record State(Set<String> shown, long cycle, long contextualDeaths, String pauseId,
                 String lastDeathId, String lastPauseId) {
        State {
            shown = Set.copyOf(shown);
            cycle = Math.max(0, cycle);
            contextualDeaths = Math.max(0, contextualDeaths);
        }
        static State empty() { return new State(Set.of(), 0, 0, "", "", ""); }
        State pause(String id) { return new State(shown, cycle, contextualDeaths, id, lastDeathId, lastPauseId); }
    }
    record Selection(DeathHint hint, boolean contextualOpportunity, long cycle) {}

    static List<DeathHint> eligible(List<DeathHint> catalogue, Predicate<String> installed) {
        return catalogue.stream().filter(h -> h.requiredMods().stream().allMatch(installed)).toList();
    }
    static Selection select(List<DeathHint> catalogue, String context, State state,
                            Predicate<String> installed, RandomGenerator random) {
        return select(catalogue, context, state, installed, random, Set.of());
    }
    static Selection select(List<DeathHint> catalogue, String context, State state,
                            Predicate<String> installed, RandomGenerator random, Set<String> reserved) {
        var eligible = eligible(catalogue, installed);
        // A temporary broken catalogue must not erase the real catalogue's history.
        if (eligible.isEmpty() || eligible.equals(List.of(DeathHints.FALLBACK)))
            return new Selection(DeathHints.FALLBACK, false, state.cycle());
        var unseen = eligible.stream().filter(h -> !state.shown().contains(h.id())).toList();
        long cycle = state.cycle();
        if (unseen.isEmpty()) { unseen = eligible; cycle++; }
        var available = unseen.stream().filter(h -> !reserved.contains(h.id())).toList();
        if (available.isEmpty() && cycle > state.cycle() && eligible.size() == 1) available = eligible;
        if (available.isEmpty()) return null; // The other surface still holds the last unseen tip.
        var avoidCurrent = available.stream().filter(h -> !h.id().equals(state.lastDeathId())
            && !h.id().equals(state.lastPauseId())).toList();
        if (!avoidCurrent.isEmpty()) available = avoidCurrent;
        var related = available.stream().filter(h -> h.contexts().contains(context)).toList();
        boolean opportunity = !related.isEmpty();
        List<DeathHint> pool;
        if (opportunity && state.contextualDeaths() % 4 != 3) pool = related;
        else {
            boolean teaser = random.nextInt(8) == 0;
            pool = available.stream().filter(h -> h.pool().equals("teaser") == teaser).toList();
            if (pool.isEmpty()) pool = available;
        }
        return new Selection(pool.get(random.nextInt(pool.size())), opportunity, cycle);
    }

    static State displayed(State state, Selection selection, boolean pause) {
        if (selection.cycle() < state.cycle()) return state;
        var shown = new HashSet<>(selection.cycle() > state.cycle() ? Set.<String>of() : state.shown());
        boolean first = shown.add(selection.hint().id());
        return new State(shown, selection.cycle(), state.contextualDeaths() +
            (first && !pause && selection.contextualOpportunity() ? 1 : 0),
            pause ? selection.hint().id() : state.pauseId(),
            pause ? state.lastDeathId() : selection.hint().id(),
            pause ? selection.hint().id() : state.lastPauseId());
    }
}
