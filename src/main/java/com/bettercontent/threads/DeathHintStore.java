package com.bettercontent.threads;

import com.google.gson.JsonObject;
import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import com.mojang.logging.LogUtils;
import net.minecraftforge.fml.loading.FMLPaths;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;

final class DeathHintStore {
    static DeathHintRotation.State load() {
        try {
            return Files.isRegularFile(path()) ? decode(JsonParser.parseString(Files.readString(path())).getAsJsonObject())
                : DeathHintRotation.State.empty();
        } catch (Exception failure) {
            LogUtils.getLogger().warn("Could not read local death-hint history", failure);
            return DeathHintRotation.State.empty();
        }
    }
    static DeathHintRotation.State decode(JsonObject json) {
        if (json.get("schema").getAsInt() != 1) throw new IllegalArgumentException("invalid death hint history");
        var recent = new ArrayList<String>();
        for (var item : json.getAsJsonArray("recent")) recent.add(item.getAsString());
        return new DeathHintRotation.State(recent, json.get("contextual_deaths").getAsLong());
    }
    static JsonObject encode(DeathHintRotation.State state) {
        var json = new JsonObject();
        json.addProperty("schema", 1);
        var recent = new JsonArray();
        state.recent().forEach(recent::add);
        json.add("recent", recent);
        json.addProperty("contextual_deaths", state.contextualDeaths());
        return json;
    }
    static void save(DeathHintRotation.State state) {
        try { Files.writeString(path(), encode(state).toString()); }
        catch (Exception failure) { LogUtils.getLogger().warn("Could not save local death-hint history", failure); }
    }
    private static Path path() { return FMLPaths.CONFIGDIR.get().resolve("better-content-threads-death-hints.json"); }
}
