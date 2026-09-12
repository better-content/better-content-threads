package com.bettercontent.threads;

import com.google.gson.JsonObject;
import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import com.mojang.logging.LogUtils;
import net.minecraftforge.fml.loading.FMLPaths;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.AtomicMoveNotSupportedException;
import java.util.HashSet;

final class DeathHintStore {
    static DeathHintRotation.State load() {
        try {
            return Files.isRegularFile(path()) ? decode(JsonParser.parseString(Files.readString(path())).getAsJsonObject())
                : DeathHintRotation.State.empty();
        } catch (Exception failure) {
            LogUtils.getLogger().warn("Could not read local tip history", failure);
            return DeathHintRotation.State.empty();
        }
    }
    static DeathHintRotation.State decode(JsonObject json) {
        int schema = json.get("schema").getAsInt();
        if (schema != 1 && schema != 2) throw new IllegalArgumentException("invalid tip history");
        var shown = new HashSet<String>();
        String last = "";
        for (var item : json.getAsJsonArray(schema == 1 ? "recent" : "shown")) {
            last = item.getAsString();
            if (!last.matches("[a-z0-9_]{3,48}")) throw new IllegalArgumentException("invalid shown tip ID");
            shown.add(last);
        }
        long count = json.get("contextual_deaths").getAsLong();
        if (schema == 1) return new DeathHintRotation.State(shown, 0, count, "", last, "");
        return new DeathHintRotation.State(shown, json.get("cycle").getAsLong(), count,
            id(json, "pause_id"), id(json, "last_death_id"), id(json, "last_pause_id"));
    }
    private static String id(JsonObject json, String key) {
        String id = json.get(key).getAsString();
        if (!id.isEmpty() && !id.matches("[a-z0-9_]{3,48}")) throw new IllegalArgumentException("invalid saved tip ID");
        return id;
    }
    static JsonObject encode(DeathHintRotation.State state) {
        var json = new JsonObject();
        json.addProperty("schema", 2);
        var shown = new JsonArray();
        state.shown().stream().sorted().forEach(shown::add);
        json.add("shown", shown);
        json.addProperty("cycle", state.cycle());
        json.addProperty("contextual_deaths", state.contextualDeaths());
        json.addProperty("pause_id", state.pauseId());
        json.addProperty("last_death_id", state.lastDeathId());
        json.addProperty("last_pause_id", state.lastPauseId());
        return json;
    }
    static void save(DeathHintRotation.State state) {
        try { write(path(), state); }
        catch (Exception failure) { LogUtils.getLogger().warn("Could not save local tip history", failure); }
    }
    static void write(Path path, DeathHintRotation.State state) throws java.io.IOException {
        Files.createDirectories(path.getParent());
        Path temporary = Files.createTempFile(path.getParent(), "tip-history-", ".json");
        try {
            Files.writeString(temporary, encode(state).toString());
            try { Files.move(temporary, path, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING); }
            catch (AtomicMoveNotSupportedException ignored) { Files.move(temporary, path, StandardCopyOption.REPLACE_EXISTING); }
        } finally { Files.deleteIfExists(temporary); }
    }
    private static Path path() { return FMLPaths.CONFIGDIR.get().resolve("better-content-threads-death-hints.json"); }
}
