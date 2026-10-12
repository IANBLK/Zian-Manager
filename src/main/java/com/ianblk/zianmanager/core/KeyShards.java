package com.ianblk.zianmanager.core;
import com.ianblk.zianmanager.storage.StorageFiles;

import com.google.gson.JsonObject;
import java.io.*;
import java.nio.file.*;
import java.util.*;

final class KeyShards {
  private final Path directory;
  private final LinkedHashMap<UUID, KeyLedger> cache = new LinkedHashMap<>(16, .75f, true);
  private final Set<UUID> pinned = new HashSet<>();
  private final int limit;

  KeyShards(Path path, JsonObject json) throws IOException {
    if (!json.keySet().equals(Set.of("schema", "layout", "directory", "cacheLimit"))
        || !json.get("layout").getAsString().equals("per-player-v1"))
      throw new IOException("Manifiesto de llaves inválido");
    String name = json.get("directory").getAsString(), prefix = path.getFileName() + ".players-";
    if (!name.startsWith(prefix)
        || !UUID.fromString(name.substring(prefix.length()))
            .toString()
            .equals(name.substring(prefix.length())))
      throw new IOException("Directorio de llaves inválido");
    directory = path.toAbsolutePath().getParent().resolve(name);
    limit = json.get("cacheLimit").getAsBigDecimal().intValueExact();
    if (limit < 1 || limit > 64 || !StorageFiles.isDirectory(directory) || StorageFiles.isSymbolicLink(directory))
      throw new IOException("Caché de llaves inválida");
  }

  synchronized KeyLedger owner(UUID player) {
    var current = cache.get(player);
    if (current != null) return current;
    try {
      Path file = directory.resolve(player + ".json");
      if (StorageFiles.isSymbolicLink(file)) throw new IOException("Enlace de llaves rechazado");
      var json = AtomicJson.read(file, JsonObject.class, null);
      if (json != null && json.get("schema").getAsInt() != 1)
        throw new IOException("Esquema de llaves de jugador inválido");
      var ledger = new KeyLedger(file);
      if (ledger.snapshot().values().stream().anyMatch(c -> !player.equals(c.player())))
        throw new IOException("Llaves de otro jugador");
      cache.put(player, ledger);
      trim();
      return ledger;
    } catch (IOException error) {
      throw new UncheckedIOException(error);
    }
  }

  private void trim() {
    var iterator = cache.entrySet().iterator();
    while (cache.size() > limit && iterator.hasNext()) {
      var entry = iterator.next();
      if (!pinned.contains(entry.getKey())) iterator.remove();
    }
  }

  synchronized KeyLedger.Cost getLoaded(UUID claim) {
    for (var ledger : cache.values()) {
      var cost = ledger.get(claim);
      if (cost != null) return cost;
    }
    return null;
  }

  synchronized void take(UUID claim, UUID player, KeyLedger.Port port) throws Exception {
    boolean alreadyPinned = pinned.contains(player);
    pinned.add(player);
    try {
      owner(player).take(claim, player, port);
    } finally {
      if (!alreadyPinned) pinned.remove(player);
      trim();
    }
  }
}
