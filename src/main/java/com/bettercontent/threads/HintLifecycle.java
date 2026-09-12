package com.bettercontent.threads;

import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.random.RandomGenerator;

/** Client-local life boundary. Respawn clears transient death state, never the Esc selection. */
final class HintLifecycle {
    private DeathHintRotation.State state;
    private DeathHintSession death = new DeathHintSession();
    private DeathHintRotation.Selection pause;
    private boolean deathProcessed;

    HintLifecycle(DeathHintRotation.State state) { this.state = state; }
    DeathHintRotation.State state() { return state; }
    boolean death(String context, long now) {
        if (deathProcessed) return false;
        deathProcessed = true;
        pause = null;
        state = state.pause("");
        death.receive(context, now);
        return true;
    }
    void respawn() { deathProcessed = false; death = new DeathHintSession(); }
    void disconnect() { respawn(); pause = null; }
    DeathHintRotation.Selection deathTip(List<DeathHint> all, Predicate<String> installed, RandomGenerator random, long now) {
        death("general", now); // Fallback when no context packet arrived.
        if (death.selection() == null) death.select(DeathHintRotation.select(all, death.freeze(now), state, installed, random,
            pause == null ? Set.of() : Set.of(pause.hint().id())));
        return death.selection();
    }
    DeathHintRotation.Selection pauseTip(List<DeathHint> all, Predicate<String> installed, RandomGenerator random) {
        var available = DeathHintRotation.eligible(all, installed);
        // A failed resource reload keeps the persisted choice until a real catalogue returns.
        if (all.equals(List.of(DeathHints.FALLBACK))) return new DeathHintRotation.Selection(DeathHints.FALLBACK, false, state.cycle());
        if (pause != null && available.contains(pause.hint())) return pause;
        var existing = available.stream().filter(h -> h.id().equals(state.pauseId())).findFirst().orElse(null);
        if (existing != null) {
            if (pause == null || !pause.hint().equals(existing)) pause = new DeathHintRotation.Selection(existing, false, state.cycle());
        } else {
            pause = DeathHintRotation.select(all, "general", state, installed, random,
                death.selection() == null ? Set.of() : Set.of(death.selection().hint().id()));

        }
        return pause;
    }
    void displayed(DeathHintRotation.Selection selection, boolean isPause) {
        if (selection != null && !selection.hint().equals(DeathHints.FALLBACK)) state = DeathHintRotation.displayed(state, selection, isPause);
    }
}
