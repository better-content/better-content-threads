package com.bettercontent.threads;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import java.util.*;

public final class ThreadDefinitions extends SimpleJsonResourceReloadListener {
    public static final ThreadDefinitions INSTANCE = new ThreadDefinitions();
    private volatile Map<String, ThreadDefinition> definitions = Map.of();
    private ThreadDefinitions() { super(new Gson(), "threads"); }
    @Override protected void apply(Map<ResourceLocation, JsonElement> resources, ResourceManager manager, ProfilerFiller profiler) {
        var loaded = new LinkedHashMap<String, ThreadDefinition>();
        resources.entrySet().stream().sorted(Map.Entry.comparingByKey()).forEach(entry -> {
            JsonElement root = entry.getValue();
            if (root.isJsonArray()) root.getAsJsonArray().forEach(e -> add(loaded, e.getAsJsonObject()));
            else add(loaded, root.getAsJsonObject());
        });
        // The reusable mod's isolated GameTest lane intentionally has no pack-owned catalogue.
        if (!loaded.isEmpty() && loaded.size() != 52) throw new IllegalStateException("Threads catalogue must contain exactly 52 definitions, found " + loaded.size());
        if(!loaded.isEmpty())for(var suit:ThreadSuit.values()){
            var orders=loaded.values().stream().filter(d->d.suit()==suit).map(ThreadDefinition::order).collect(java.util.stream.Collectors.toSet());
            var expected=java.util.stream.IntStream.rangeClosed(1,13).boxed().collect(java.util.stream.Collectors.toSet());
            if(!orders.equals(expected))throw new IllegalStateException("Thread suit must contain orders 1..13: "+suit.id());
        }
        definitions = Collections.unmodifiableMap(loaded);
    }
    private static void add(Map<String, ThreadDefinition> loaded, JsonObject json) {
        var definition = ThreadDefinition.parse(json);
        var approved=ThreadArt.BY_ID.get(definition.id());
        if(approved==null||approved.aspect()!=definition.aspect()||approved.suit()!=definition.suit()||approved.order()!=definition.order()||approved.future()!=definition.future())
            throw new IllegalStateException("unapproved thread identity or assignment for " + definition.id());
        if (loaded.putIfAbsent(definition.id(), definition) != null) throw new IllegalStateException("duplicate thread " + definition.id());
    }
    public Collection<ThreadDefinition> all() { return definitions.values(); }
    public ThreadDefinition get(String id) { return definitions.get(id); }
    public boolean contains(String id) { return definitions.containsKey(id); }
}
