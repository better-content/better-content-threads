package com.bettercontent.threads;

import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;

record LoadingBrief(String id, String headline, String body, ResourceLocation art) {
    LoadingBrief {
        if (!id.matches("[a-z0-9_]{3,48}")) throw new IllegalArgumentException("invalid loading brief id");
        if (headline.isBlank() || headline.length() > 54) throw new IllegalArgumentException("invalid loading brief headline");
        int words = body.trim().split("\\s+").length;
        if (words < 25 || words > 40) throw new IllegalArgumentException("loading brief body must contain 25-40 words");
    }

    static LoadingBrief parse(JsonObject json) {
        return new LoadingBrief(
            json.get("id").getAsString(),
            json.get("headline").getAsString(),
            json.get("body").getAsString(),
            ResourceLocation.tryParse(json.get("art").getAsString())
        );
    }
}
