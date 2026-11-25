package com.ianblk.zianmanager;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.gson.JsonParser;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Pattern;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

class LangFilesTest {
    private static final Pattern PLACEHOLDER = Pattern.compile("%(\\d+\\$)?[sd]");

    private static Map<String, String> load(String name) throws Exception {
        var json = JsonParser.parseString(new String(
            LangFilesTest.class.getResourceAsStream("/assets/zianmanager/lang/" + name).readAllBytes(), StandardCharsets.UTF_8)).getAsJsonObject();
        var map = new TreeMap<String, String>();
        json.entrySet().forEach(e -> map.put(e.getKey(), e.getValue().getAsString()));
        return map;
    }
    private static String placeholders(String text) {
        var found = new java.util.ArrayList<String>();
        var m = PLACEHOLDER.matcher(text);
        while (m.find()) found.add(m.group());
        java.util.Collections.sort(found);
        return String.join(",", found);
    }

    @Test void allLanguagesHaveSameKeysAndPlaceholders() throws Exception {
        var es = load("es_es.json");
        for (var other : new String[] {"en_us.json", "es_mx.json"}) {
            var map = load(other);
            assertEquals(es.keySet(), map.keySet(), "Claves distintas en " + other);
            es.forEach((key, text) -> assertEquals(placeholders(text), placeholders(map.get(key)), key + " en " + other));
        }
    }

    private static boolean uiKey(String key) { return key.startsWith("zianmanager.ui.") || key.startsWith("zianmanager.msg."); }
    /** Textos que se escriben igual en inglés (nombres propios, siglas o palabras iguales). */
    private static final Set<String> SAME_IN_ENGLISH = Set.of("DEFAULT", "VIP", "General", "Dungeon", "Mobs", "No", "Zian Manager");

    @Test void adminUiKeysAreTranslatedAndMexicanEqualsSpanish() throws Exception {
        var es = load("es_es.json");
        var en = load("en_us.json");
        var mx = load("es_mx.json");
        int count = 0;
        for (var entry : es.entrySet()) {
            if (!uiKey(entry.getKey())) continue;
            count++;
            assertEquals(entry.getValue(), mx.get(entry.getKey()), "es_mx debe ser igual a es_es: " + entry.getKey());
            assertFalse(en.get(entry.getKey()).isBlank(), "en_us vacío: " + entry.getKey());
            if (!SAME_IN_ENGLISH.contains(entry.getValue()))
                assertTrue(!en.get(entry.getKey()).equals(entry.getValue()), "en_us sin traducir: " + entry.getKey());
            assertTrue(entry.getKey().matches("[a-z0-9_.]+"), "clave no legible: " + entry.getKey());
        }
        assertTrue(count > 300, "faltan claves de la interfaz de admin: " + count);
    }

    /** Todo Ui.t("...") del cliente y los ids de pestaña y títulos de EditorSections deben tener clave con ese español exacto. */
    @Test void textsUsedByTheClientHaveKeys() throws Exception {
        var known = new java.util.HashSet<String>();
        load("es_es.json").forEach((key, text) -> { if (uiKey(key) || key.startsWith("ui.zianmanager.")) known.add(text); });
        var literal = Pattern.compile("Ui\\.[tc]\\(\"((?:[^\"\\\\]|\\\\.)*)\"");
        var tabs = Pattern.compile("m\\.put\\(\"([^\"]+)\"");
        var titles = Pattern.compile("case \"[a-z]+\"->Ui\\.t\\(\"([^\"]+)\"");
        var missing = new java.util.ArrayList<String>();
        var dir = Path.of("src/main/java/com/ianblk/zianmanager/client");
        try (var files = Files.list(dir)) {
            for (var file : files.filter(f -> f.toString().endsWith(".java")).toList()) {
                var source = Files.readString(file, StandardCharsets.UTF_8);
                var all = new java.util.ArrayList<String>();
                for (var p : new Pattern[] {literal, titles}) { var m = p.matcher(source); while (m.find()) all.add(m.group(1)); }
                if (file.getFileName().toString().equals("EditorSections.java")) { var m = tabs.matcher(source); while (m.find()) all.add(m.group(1)); }
                for (var text : all) if (!known.contains(text.replace("\\\"", "\""))) missing.add(file.getFileName() + ": " + text);
            }
        }
        assertEquals(java.util.List.of(), missing, "Textos sin clave en es_es");
    }
}
