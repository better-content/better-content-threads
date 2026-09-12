package com.bettercontent.threads;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.random.RandomGenerator;

/** Each caller supplies its own surface history; only rendering records exposure. */
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
        return select(catalogue, context, "death", state, installed, random, reserved);
    }
    static Selection select(List<DeathHint> catalogue, String context, String surface, State state,
                            Predicate<String> installed, RandomGenerator random, Set<String> reserved) {
        var eligible = eligible(catalogue, installed).stream().filter(h -> h.surfaces().contains(surface))
            .filter(h -> h.requirements().isEmpty() || h.requirements().contains(context)).toList();
        var related = eligible.stream().filter(h -> h.contexts().contains(context)).toList();
        // A useful explanation remains relevant even when it has been shown before.
        if (!related.isEmpty()) eligible = related;
        else if (!surface.equals("menu")) eligible = eligible.stream().filter(h -> h.contexts().isEmpty()).toList();
        if (eligible.isEmpty()) return new Selection(DeathHints.FALLBACK, false, state.cycle());
        var unseen = eligible.stream().filter(h -> !state.shown().contains(h.id())).toList();
        long cycle = state.cycle();
        if (unseen.isEmpty()) unseen = eligible;
        var available = unseen.stream().filter(h -> !reserved.contains(h.id())).toList();
        if (available.isEmpty()) available = unseen;
        var avoid = available.stream().filter(h -> !h.id().equals(state.lastDeathId()) && !h.id().equals(state.lastPauseId())).toList();
        if (!avoid.isEmpty()) available = avoid;
        return new Selection(available.get(random.nextInt(available.size())), !related.isEmpty(), cycle);
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
