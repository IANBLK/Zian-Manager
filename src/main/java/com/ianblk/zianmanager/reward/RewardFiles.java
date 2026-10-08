package com.ianblk.zianmanager.reward;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParser;
import com.google.gson.JsonElement;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;

final class RewardFiles {
    static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    private RewardFiles() {}
    static JsonElement read(Path file, long limit) throws IOException {
        if (Files.size(file) > limit) throw new IOException("Archivo de recompensas demasiado grande: " + file);
        try (var reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            return JsonParser.parseReader(reader);
        } catch (RuntimeException error) { throw new IOException("JSON inválido: " + file, error); }
    }
    static void write(Path file, Object data) throws IOException {
        com.ianblk.zianmanager.core.AtomicJson.write(file,data);
    }
}
