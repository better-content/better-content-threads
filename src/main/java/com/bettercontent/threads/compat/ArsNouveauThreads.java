package com.bettercontent.threads.compat;

import com.bettercontent.threads.ThreadSignals;
import com.hollingsworth.arsnouveau.api.event.EffectResolveEvent;
import com.hollingsworth.arsnouveau.api.spell.Spell;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;

/** Exact spell-save to matching resolved-effect correlation for Ars 4.12.7. */
public final class ArsNouveauThreads {
    private static final String ROOT = "BetterContentThreadsArsEpisode";
    private ArsNouveauThreads() {}

    public static void authored(ServerPlayer player, Spell spell) {
        if (spell == null || !spell.isValid() || spell.getSpellSize() < 2) return;
        String signature = signature(spell);
        remember(player, "reality", signature, "spell_authored", "ars_nouveau");
        Set<String> namespaces = namespaces(spell);
        if (namespaces.contains("ars_elemental")) remember(player, "elements", signature, "spell_element", "added");
        if (crossing(namespaces) != null) remember(player, "traditions", signature, "tradition_cross", "constructed");
    }

    @SubscribeEvent
    public static void effectResolved(EffectResolveEvent.Post event) {
        if (!(event.shooter instanceof ServerPlayer player) || event.spell == null) return;
        String signature = signature(event.spell);
        complete(player, "reality", "reality_has_grammar", signature, "spell_effect", "ars_nouveau");
        if (namespaces(event.spell).contains("ars_elemental"))
            complete(player, "elements", "elements_change_sentence", signature, "spell_element", "triggered");
        String crossing = crossing(namespaces(event.spell));
        if (crossing != null)
            complete(player, "traditions", "traditions_can_cross", signature, "tradition_cross", crossing);
    }

    private static void remember(ServerPlayer player, String key, String signature, String type, String value) {
        String token = player.getUUID() + ":ars:" + key + ":" + Integer.toUnsignedString(signature.hashCode(), 36)
            + ":" + player.server.getTickCount();
        CompoundTag state = state(player);
        state.putString(key + "Signature", signature);
        state.putString(key + "Token", token);
        save(player, state);
        ThreadSignals.emit(player, type, value, token);
    }

    private static void complete(ServerPlayer player, String key, String card, String signature, String type, String value) {
        CompoundTag state = state(player);
        String token = state.getString(key + "Token");
        if (!signature.equals(state.getString(key + "Signature")) || token.isBlank()) return;
        String active = ThreadSignals.activeCorrelation(player, card);
        if (!token.equals(active)) return;
        ThreadSignals.emit(player, type, value, token);
        state.remove(key + "Signature");
        state.remove(key + "Token");
        save(player, state);
    }

    private static String signature(Spell spell) {
        return spell.serializeRecipe().stream().map(Object::toString).reduce((a, b) -> a + "," + b).orElse("");
    }

    private static Set<String> namespaces(Spell spell) {
        var result = new LinkedHashSet<String>();
        spell.serializeRecipe().forEach(id -> result.add(id.getNamespace().toLowerCase(Locale.ROOT)));
        return Set.copyOf(result);
    }

    private static String crossing(Set<String> namespaces) {
        if (!namespaces.contains("ars_nouveau")) return null;
        if (namespaces.contains("ars_creo")) return "ars_creo";
        if (namespaces.contains("arseng")) return "ars_energistique";
        return null;
    }

    private static CompoundTag state(ServerPlayer player) {
        return player.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG).getCompound(ROOT);
    }

    private static void save(ServerPlayer player, CompoundTag state) {
        CompoundTag persisted = player.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG);
        persisted.put(ROOT, state);
        player.getPersistentData().put(Player.PERSISTED_NBT_TAG, persisted);
    }
}
