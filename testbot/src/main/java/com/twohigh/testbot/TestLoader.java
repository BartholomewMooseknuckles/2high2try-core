package com.twohigh.testbot;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class TestLoader {

    private TestLoader() {}

    /** Ordered map of section title -> tests in that section. Numbers are global and sequential. */
    public static Map<String, List<TestDefinition>> load() {
        try (InputStream in = TestLoader.class.getResourceAsStream("/tests.json")) {
            JsonArray sections = JsonParser
                    .parseReader(new InputStreamReader(in, StandardCharsets.UTF_8))
                    .getAsJsonArray();

            Map<String, List<TestDefinition>> result = new LinkedHashMap<>();
            int num = 0;
            for (var secEl : sections) {
                JsonObject sec = secEl.getAsJsonObject();
                String title = sec.get("title").getAsString();
                String badge = sec.get("badge").getAsString();
                List<TestDefinition> tests = new ArrayList<>();
                for (var tEl : sec.getAsJsonArray("tests")) {
                    JsonObject t = tEl.getAsJsonObject();
                    num++;
                    tests.add(new TestDefinition(
                            num, title, badge,
                            t.get("a").getAsString(),
                            t.get("e").getAsString(),
                            t.has("known") ? t.get("known").getAsString() : null));
                }
                result.put(title, tests);
            }
            return result;
        } catch (Exception e) {
            throw new IllegalStateException("Failed to load tests.json", e);
        }
    }
}
