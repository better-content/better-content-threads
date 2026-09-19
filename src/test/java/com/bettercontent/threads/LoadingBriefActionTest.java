package com.bettercontent.threads;

import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

class LoadingBriefActionTest {
    @Test void everyLoadingBriefProvidesAnActionableMechanic() {
        try (var stream = getClass().getResourceAsStream("/assets/better_content_threads/loading_briefs/catalogue.json")) {
            assertNotNull(stream);
            var briefs = JsonParser.parseReader(new InputStreamReader(stream, StandardCharsets.UTF_8))
                    .getAsJsonObject().getAsJsonArray("briefs");
            for (var value : briefs) {
                var action = value.getAsJsonObject().get("action").getAsString().toLowerCase();
                assertFalse(action.startsWith("read ") || action.startsWith("open lessons"), action);
                assertTrue(action.length() >= 20, action);
            }
        } catch (Exception failure) {
            fail(failure);
        }
    }
}
