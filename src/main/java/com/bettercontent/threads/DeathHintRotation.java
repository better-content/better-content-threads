package com.bettercontent.threads;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.random.RandomGenerator;

final class DeathHintRotation {
    record State(List<String> recent, long contextualDeaths) {
        State {
            recent = List.copyOf(recent.subList(Math.max(0, recent.size() - 12), recent.size()));
            contextualDeaths = Math.max(0, contextualDeaths);
        }
        static State empty() { return new State(List.of(), 0); }
    }
    record Selection(DeathHint hint, boolean contextualOpportunity) {}

    static Selection select(List<DeathHint> catalogue, String context, State state,
                            Predicate<String> installed, RandomGenerator random) {
        var eligible = catalogue.stream().filter(h -> h.requiredMods().stream().allMatch(installed)).toList();
        var related = eligible.stream().filter(h -> h.contexts().contains(context)).toList();
        boolean opportunity = !related.isEmpty();
        List<DeathHint> pool;
        if (opportunity && state.contextualDeaths() % 4 != 3) {
            pool = related;
        } else {
            boolean teaser = random.nextInt(8) == 0;
            pool = eligible.stream().filter(h -> h.pool().equals("teaser") == teaser).toList();
            if (pool.isEmpty()) pool = eligible;
        }
        if (pool.isEmpty()) pool = List.of(DeathHints.FALLBACK);
        var fresh = pool.stream().filter(h -> !state.recent().contains(h.id())).toList();
        if (!fresh.isEmpty()) pool = fresh;
        else {
            // Exhausted small contextual pools repeat the least recently displayed hint.
            int oldest = pool.stream().mapToInt(h -> state.recent().indexOf(h.id())).min().orElse(-1);
            pool = pool.stream().filter(h -> state.recent().indexOf(h.id()) == oldest).toList();
        }
        return new Selection(pool.get(random.nextInt(pool.size())), opportunity);
    }

    static State displayed(State state, Selection selection) {
        var recent = new ArrayList<>(state.recent());
        recent.remove(selection.hint().id());
        recent.add(selection.hint().id());
        return new State(recent, state.contextualDeaths() + (selection.contextualOpportunity() ? 1 : 0));
    }
}
