package com.ianblk.zianmanager.core;
import com.ianblk.zianmanager.storage.StorageFiles;

import com.google.gson.*;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.*;
import java.util.*;

/** Offline migration retaining every PAID/RESERVED/APPLYING/REVIEW entry. */
public final class KeyStorageMaintenance {
  public static Path migrateOffline(Path path) throws IOException {
    path = path.toAbsolutePath().normalize();
    if (StorageFiles.isSymbolicLink(path)) throw new IOException("Symlink key ledger refused");
    boolean exists = StorageFiles.exists(path);
    if (exists && StorageFiles.size(path) > 16777216) throw new IOException("Oversized key ledger");
    byte[] before = exists ? StorageFiles.readAllBytes(path) : null;
    var old = new KeyLedger(path);
    var snapshot = old.snapshot();
    var groups = new LinkedHashMap<UUID, Map<UUID, KeyLedger.Cost>>();
    snapshot.forEach(
        (id, cost) ->
            groups.computeIfAbsent(cost.player(), p -> new LinkedHashMap<>()).put(id, cost));
    Path parent = path.getParent();
    StorageFiles.createDirectories(parent);
    String token = UUID.randomUUID().toString(), name = path.getFileName() + ".players-" + token;
    Path stage = parent.resolve(name + ".staging"),
        directory = parent.resolve(name),
        marker = path.resolveSibling(path.getFileName() + ".maintenance");
    boolean ownsMarker = false;
    try {
      StorageFiles.writeString(
          marker, "Offline key migration; directory=" + name, StandardOpenOption.CREATE_NEW);
      ownsMarker = true;
      forceFile(marker);
      forceDirectory(parent);
      StorageFiles.createDirectory(stage);
      for (var entry : groups.entrySet()) {
        Path target = stage.resolve(entry.getKey() + ".json");
        AtomicJson.write(target, new KeyLedger.Data(1, entry.getValue()));
        if (!new KeyLedger(target).snapshot().equals(entry.getValue()))
          throw new IOException("Key shard verification failed");
      }
      forceDirectory(stage);
      if (exists) {
        if (!Arrays.equals(before, StorageFiles.readAllBytes(path)))
          throw new IOException("Keys changed; server must be offline");
        Path archive = parent.resolve(path.getFileName() + ".archive-" + token);
        StorageFiles.write(archive, before, StandardOpenOption.CREATE_NEW);
        forceFile(archive);
        if (!Arrays.equals(before, StorageFiles.readAllBytes(archive)))
          throw new IOException("Key archive verification failed");
      } else if (StorageFiles.exists(path)) throw new IOException("Keys appeared; server must be offline");
      forceDirectory(parent);
      StorageFiles.move(stage, directory, StandardCopyOption.ATOMIC_MOVE);
      forceDirectory(parent);
      var manifest = new JsonObject();
      manifest.addProperty("schema", 2);
      manifest.addProperty("layout", "per-player-v1");
      manifest.addProperty("directory", name);
      manifest.addProperty("cacheLimit", 32);
      AtomicJson.write(path, manifest);
    } finally {
      if (ownsMarker) {
        StorageFiles.deleteIfExists(marker);
        forceDirectory(parent);
      }
    }
    return directory;
  }

  private static void forceFile(Path path) throws IOException {
    try (var c = StorageFiles.channel(path, StandardOpenOption.WRITE)) {
      c.force(true);
    }
  }

  private static void forceDirectory(Path path) throws IOException {
    if (!System.getProperty("os.name").toLowerCase(Locale.ROOT).contains("windows"))
      try (var c = StorageFiles.channel(path, StandardOpenOption.READ)) {
        c.force(true);
      }
  }

  public static void main(String[] args) throws Exception {
    if (args.length != 2 || !args[0].equals("migrate-offline"))
      throw new IllegalArgumentException(
          "Usage: migrate-offline <keys.json>; stop server and disable restart first");
    System.out.println(migrateOffline(Path.of(args[1])));
  }
}
