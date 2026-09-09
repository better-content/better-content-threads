package com.bettercontent.threads;

import com.google.gson.JsonParser;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import org.slf4j.Logger;
import com.mojang.logging.LogUtils;

final class LoadingBriefs implements ResourceManagerReloadListener {
    private static final Logger LOGGER = LogUtils.getLogger();
    static final ResourceLocation MANIFEST = new ResourceLocation(BetterContentThreads.MOD_ID, "loading_briefs/catalogue.json");
    static final LoadingBrief FALLBACK = new LoadingBrief("threads", "Orientation", "Your discoveries live in Threads",
        "When the world teaches you a changed rule, a small Threads reminder appears. Press the shown key — M by default — to read the rule and its evidence.",
        "Open Threads when a new rule appears.",
        new ResourceLocation(BetterContentThreads.MOD_ID, "textures/gui/loading_briefs/threads.png"));
    static final LoadingBriefs INSTANCE = new LoadingBriefs();
    private volatile List<LoadingBrief> all = List.of(FALLBACK);

    private LoadingBriefs() {}

    List<LoadingBrief> all() {
        return all;
    }

    @Override
    public void onResourceManagerReload(ResourceManager resources) {
        try (var reader = new InputStreamReader(resources.getResourceOrThrow(MANIFEST).open(), StandardCharsets.UTF_8)) {
            var root = JsonParser.parseReader(reader).getAsJsonObject();
            if (!"bc.loading_briefs.v2".equals(root.get("schema").getAsString())) throw new IllegalArgumentException("invalid loading brief schema");
            var parsed = new ArrayList<LoadingBrief>();
            var ids = new HashSet<String>();
            for (var element : root.getAsJsonArray("briefs")) {
                var brief = LoadingBrief.parse(element.getAsJsonObject());
                if (!ids.add(brief.id())) throw new IllegalArgumentException("duplicate loading brief: " + brief.id());
                parsed.add(brief);
            }
            if (parsed.size() != 16 || !parsed.get(0).id().equals("threads")) throw new IllegalArgumentException("loading briefs require the Threads introduction plus fifteen survival lessons");
            all = List.copyOf(parsed);
        } catch (Exception failure) {
            LOGGER.error("Could not load learning briefs; using the built-in Threads introduction", failure);
            all = List.of(FALLBACK);
        }
    }
}
