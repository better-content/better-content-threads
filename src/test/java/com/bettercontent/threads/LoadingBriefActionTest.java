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
            boolean foundThreads = false;
            for (var value : briefs) {
                var object = value.getAsJsonObject();
                if ("threads".equals(object.get("id").getAsString())) {
                    foundThreads = true;
                    assertTrue(object.get("body").getAsString().contains("hunger"));
                    assertTrue(object.get("action").getAsString().contains("food"));
                }
                var action = object.get("action").getAsString().toLowerCase();
                assertFalse(action.contains("open lessons") || action.contains("read the"), action);
                assertTrue(action.split("\\s+").length >= 4, action);
            }
            assertTrue(foundThreads);
        } catch (Exception failure) {
            fail(failure);
        }
    }
}
