package com.ianblk.zianmanager.core;
import com.ianblk.zianmanager.storage.StorageFiles;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

public final class KeyLedger {
  public record Cost(UUID player, String key, String phase, String evidence) {
    public Cost {
      Objects.requireNonNull(player);
      if (key == null
          || (!key.isBlank() && !key.matches("zianmanager:(common|rare|epic|legendary)_key"))
          || !Set.of("RESERVED", "APPLYING", "PAID", "REVIEW").contains(phase))
        throw new IllegalArgumentException("Coste inválido");
    }
  }

  public record Data(int schema, Map<UUID, Cost> entries) {
    public Data {
      if (schema != 1 || entries.size() > 200000)
        throw new IllegalArgumentException("Diario inválido");
      entries = Map.copyOf(entries);
    }
  }

  public interface Port {
    boolean available(String key);

    boolean consumeAndSave(String key) throws Exception;
  }

  private final Path path;
  private Data data;
  private final KeyShards shards;

  public KeyLedger(Path path) throws IOException {
    this.path = path;
    if (StorageFiles.exists(path.resolveSibling(path.getFileName() + ".maintenance")))
      throw new IOException("Mantenimiento de llaves en curso");
    try {
      var json = AtomicJson.read(path, com.google.gson.JsonObject.class, null);
      if (json != null
          && json.has("schema")
          && json.get("schema").getAsBigDecimal().intValueExact() == 2) {
        shards = new KeyShards(path, json);
        data = null;
      } else {
        shards = null;
        data = json == null ? new Data(1, Map.of()) : AtomicJson.GSON.fromJson(json, Data.class);
      }
    } catch (RuntimeException error) {
      throw new IOException("Llaves ilegibles; archivo conservado", error);
    }
  }

  public Cost get(UUID claim) {
    return shards == null ? data.entries().get(claim) : shards.getLoaded(claim);
  }

  public Cost get(UUID player, UUID claim) {
    if (shards != null) return shards.owner(player).get(player, claim);
    var value = get(claim);
    return value != null && player.equals(value.player()) ? value : null;
  }

  Map<UUID, Cost> snapshot() {
    if (shards != null) throw new IllegalStateException("El registro ya está separado por jugador");
    return data.entries();
  }

  public Cost bind(UUID claim, UUID player, String key) throws IOException {
    if (shards != null) return shards.owner(player).bind(claim, player, key);
    var old = get(claim);
    if (old != null) {
      if (!old.player().equals(player)) throw new IllegalArgumentException("Dueño incorrecto");
      return old;
    }
    var cost = new Cost(player, key, key.isEmpty() ? "PAID" : "RESERVED", "");
    put(claim, cost);
    return cost;
  }

  public void take(UUID claim, UUID player, Port port) throws Exception {
    if (shards != null) {
      shards.take(claim, player, port);
      return;
    }
    var c = get(claim);
    if (c == null) return;
    if (!c.player().equals(player)) throw new IllegalArgumentException("Dueño incorrecto");
    if (c.phase().equals("PAID")) return;
    if (!c.phase().equals("RESERVED"))
      throw new IllegalArgumentException("Llave pendiente de revisión; no se consumirá otra.");
    if (!port.available(c.key()))
      throw new IllegalArgumentException("Necesitas la llave correspondiente en tu inventario.");
    put(claim, new Cost(player, c.key(), "APPLYING", "consumption_started"));
    boolean confirmed = false;
    try {
      confirmed = port.consumeAndSave(c.key());
    } finally {
      put(
          claim,
          new Cost(
              player,
              c.key(),
              confirmed ? "PAID" : "REVIEW",
              confirmed ? "inventory_readback" : "unconfirmed"));
    }
    if (!confirmed)
      throw new IllegalArgumentException("Consumo de llave en revisión; loot conservado.");
  }

  public void resolve(UUID claim, UUID player, String evidence) throws IOException {
    if (shards != null) {
      shards.owner(player).resolve(claim, player, evidence);
      return;
    }
    var c = get(claim);
    if (c == null
        || !c.player().equals(player)
        || !Set.of("REVIEW", "APPLYING").contains(c.phase())
        || evidence == null
        || evidence.length() < 8
        || evidence.length() > 500)
      throw new IllegalArgumentException("Revisión o evidencia inválida");
    put(claim, new Cost(player, c.key(), "PAID", evidence));
  }

  private void put(UUID claim, Cost cost) throws IOException {
    if (StorageFiles.exists(path.resolveSibling(path.getFileName() + ".maintenance")))
      throw new IOException("Mantenimiento de llaves en curso");
    var map = new HashMap<>(data.entries());
    map.put(claim, cost);
    var next = new Data(1, map);
    AtomicJson.write(path, next);
    data = next;
  }
}
