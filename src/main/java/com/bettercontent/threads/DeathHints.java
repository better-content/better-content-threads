package com.bettercontent.threads;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

final class DeathHints implements ResourceManagerReloadListener {
    static final DeathHint FALLBACK = new DeathHint("review_controls", "controls.pack_specific_actions", "discovery",
        "Check Options > Controls for your current bindings. Practice unfamiliar actions somewhere safe.",
        Set.of(), Set.of(), List.of("minecraft: OptionsScreen / KeyBindsScreen"));
    static final DeathHints INSTANCE = new DeathHints();
    private volatile List<DeathHint> all = List.of(FALLBACK);
    List<DeathHint> all() { return all; }

    static List<DeathHint> parse(JsonObject root) {
        if (!"bc.teaching_hints.v2".equals(root.get("schema").getAsString()))
            throw new IllegalArgumentException("invalid death hint schema");
        var hints = new ArrayList<DeathHint>();
        var ids = new HashSet<String>();
        for (var entry : root.getAsJsonArray("hints")) {
            var hint = DeathHint.parse(entry.getAsJsonObject());
            if (!ids.add(hint.id())) throw new IllegalArgumentException("duplicate death hint: " + hint.id());
            hints.add(hint);
        }
        if (hints.isEmpty() || hints.size() > 512) throw new IllegalArgumentException("invalid death hint count");
        return List.copyOf(hints);
    }

    @Override public void onResourceManagerReload(ResourceManager resources) {
        try (var reader = new InputStreamReader(resources.getResourceOrThrow(new ResourceLocation(
                BetterContentThreads.MOD_ID, "death_hints/catalogue.json")).open(), StandardCharsets.UTF_8)) {
            all = parse(JsonParser.parseReader(reader).getAsJsonObject());
        } catch (Exception failure) {
            LogUtils.getLogger().error("Could not load death hints; using built-in general advice", failure);
            all = List.of(FALLBACK);
        }
    }
}
