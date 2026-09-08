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
    private static final Path PATH = FMLPaths.CONFIGDIR.get().resolve("better-content-threads-learning.json");

    private LoadingBriefStore() {}

    static LoadingBriefRotation.State load() {
        try {
            if (!Files.isRegularFile(PATH)) return new LoadingBriefRotation.State(java.util.Set.of(), "");
            var json = GSON.fromJson(Files.readString(PATH, StandardCharsets.UTF_8), JsonObject.class);
            var seen = new LinkedHashSet<String>();
            if (json != null && json.has("seen")) for (var value : json.getAsJsonArray("seen")) seen.add(value.getAsString());
            return new LoadingBriefRotation.State(seen, json != null && json.has("last") ? json.get("last").getAsString() : "");
        } catch (Exception failure) {
            LOGGER.warn("Could not read the local loading-brief history", failure);
            return new LoadingBriefRotation.State(java.util.Set.of(), "");
        }
    }

    static void save(LoadingBriefRotation.State state) {
        try {
            var json = new JsonObject();
            var seen = new com.google.gson.JsonArray();
            state.seen().forEach(seen::add);
            json.add("seen", seen);
            json.addProperty("last", state.last());
            Files.writeString(PATH, GSON.toJson(json), StandardCharsets.UTF_8);
        } catch (Exception failure) {
            LOGGER.warn("Could not save the local loading-brief history", failure);
        }
    }
}
