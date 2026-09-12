package com.bettercontent.threads;

import com.google.gson.JsonObject;
import java.util.List;
import java.util.Set;

/** Curated copy and authoring provenance; no discovery or reward state. */
record DeathHint(String id, String conceptId, String pool, String text,
                 Set<String> contexts, Set<String> requiredMods, List<String> sources) {
    DeathHint {
        if (!id.matches("[a-z0-9_]{3,48}") || !conceptId.matches("[a-z0-9_.]{3,80}"))
            throw new IllegalArgumentException("invalid death hint identity");
        if (!Set.of("survival", "discovery", "teaser").contains(pool))
            throw new IllegalArgumentException("invalid death hint pool");
        if (text.isBlank() || text.length() > 240 || text.trim().split("\\s+").length > 40)
            throw new IllegalArgumentException("death hint must fit 40 words / 240 characters");
        String plain = text.replace("{sneak}", "").replace("{use}", "").replace("{threads}", "");
        if (plain.contains("{") || plain.contains("}")) throw new IllegalArgumentException("unknown hint binding");
        contexts = Set.copyOf(contexts);
        requiredMods = Set.copyOf(requiredMods);
        sources = List.copyOf(sources);
        if (!DeathHintContext.CATEGORIES.containsAll(contexts) || contexts.contains("general"))
            throw new IllegalArgumentException("unknown hint context");
        if (requiredMods.stream().anyMatch(mod -> !mod.matches("[a-z][a-z0-9_]{1,63}")))
            throw new IllegalArgumentException("invalid required mod");
        if (sources.isEmpty() || sources.stream().anyMatch(String::isBlank))
            throw new IllegalArgumentException("hint needs mechanical sources");
    }

    static DeathHint parse(JsonObject json) {
        return new DeathHint(json.get("id").getAsString(), json.get("concept_id").getAsString(),
            json.get("pool").getAsString(), json.get("text").getAsString(),
            Set.copyOf(strings(json, "contexts")), Set.copyOf(strings(json, "required_mods")), strings(json, "sources"));
    }

    private static List<String> strings(JsonObject json, String key) {
        var result = new java.util.ArrayList<String>();
        for (var item : json.getAsJsonArray(key)) result.add(item.getAsString());
        return result;
    }
}
