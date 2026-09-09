package com.bettercontent.threads;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import net.minecraftforge.fml.loading.FMLPaths;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashSet;
import org.slf4j.Logger;
import com.mojang.logging.LogUtils;

final class LoadingBriefStore {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Gson GSON = new Gson();
    private LoadingBriefStore() {}

    static LoadingBriefRotation.State load() {
        try {
            Path path = path();
            if (!Files.isRegularFile(path)) return new LoadingBriefRotation.State(java.util.Set.of(), "");
            var json = GSON.fromJson(Files.readString(path, StandardCharsets.UTF_8), JsonObject.class);
            return decode(json);
        } catch (Exception failure) {
            LOGGER.warn("Could not read the local loading-brief history", failure);
            return new LoadingBriefRotation.State(java.util.Set.of(), "");
        }
    }

    static LoadingBriefRotation.State decode(JsonObject json) {
        var seen = new LinkedHashSet<String>();
        if (json != null && json.has("seen")) for (var value : json.getAsJsonArray("seen")) seen.add(value.getAsString());
        return new LoadingBriefRotation.State(seen, json != null && json.has("last") ? json.get("last").getAsString() : "");
    }

    static void save(LoadingBriefRotation.State state) {
        try {
            var json = new JsonObject();
            json.addProperty("schema", 2);
            var seen = new com.google.gson.JsonArray();
            state.seen().forEach(seen::add);
            json.add("seen", seen);
            json.addProperty("last", state.last());
            Files.writeString(path(), GSON.toJson(json), StandardCharsets.UTF_8);
        } catch (Exception failure) {
            LOGGER.warn("Could not save the local loading-brief history", failure);
        }
    }

    private static Path path() {
        return FMLPaths.CONFIGDIR.get().resolve("better-content-threads-learning.json");
    }
}
