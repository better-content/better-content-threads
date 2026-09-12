package com.bettercontent.threads;

import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;

record LoadingBrief(
        String id,
        String conceptId,
        String owner,
        String relatedThread,
        String category,
        String headline,
        String body,
        String action,
        ResourceLocation art
) {
    LoadingBrief {
        if (!id.matches("[a-z0-9_]{3,48}")) throw new IllegalArgumentException("invalid loading brief id");
        if (!conceptId.matches("[a-z0-9_.]{3,80}")) throw new IllegalArgumentException("invalid loading brief concept");
        if (!owner.matches("[a-z0-9_.-]{1,80}")) throw new IllegalArgumentException("invalid loading brief owner");
        relatedThread = relatedThread == null ? "" : relatedThread;
        if (!relatedThread.isEmpty() && !relatedThread.matches("[a-z0-9_]{3,48}")) {
            throw new IllegalArgumentException("invalid related thread");
        }
        if (!category.matches("[A-Za-z ]{3,24}")) throw new IllegalArgumentException("invalid loading brief category");
        if (headline.isBlank() || headline.length() > 54) throw new IllegalArgumentException("invalid loading brief headline");
        int words = body.trim().split("\\s+").length;
        if (body.isBlank() || words > 48) throw new IllegalArgumentException("loading brief body must contain 1-48 words");
        int actionWords = action.trim().split("\\s+").length;
        if (actionWords < 3 || actionWords > 16 || action.length() > 96) {
            throw new IllegalArgumentException("loading brief action must contain 3-16 words and at most 96 characters");
        }
        if (art == null) throw new IllegalArgumentException("invalid loading brief art");
    }

    static LoadingBrief parse(JsonObject json) {
        return new LoadingBrief(
            json.get("id").getAsString(),
            json.get("concept_id").getAsString(),
            json.get("owner").getAsString(),
            json.has("related_thread") ? json.get("related_thread").getAsString() : "",
            json.get("category").getAsString(),
            json.get("headline").getAsString(),
            json.get("body").getAsString(),
            json.get("action").getAsString(),
            ResourceLocation.tryParse(json.get("art").getAsString())
        );
    }
}
