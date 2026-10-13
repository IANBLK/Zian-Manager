package com.ianblk.zianmanager.core;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

/** Stable dungeon identities; names and captured arrival points may change independently. */
public final class DungeonEntrances {
  public static final String PREFIX = "@entrada ";
  public record Arrival(String id, String name, String dimension, Definitions.Point position) {
    public Arrival {
      Definitions.id(id); Definitions.resource(dimension);
      if (name == null || name.isBlank() || name.length() > 64 || position == null)
        throw new IllegalArgumentException("Punto de llegada inválido");
    }
  }
  public record Dungeon(String id, String name, List<Arrival> points) {
    public Dungeon {
      Definitions.id(id);
      if (name == null || name.isBlank() || name.length() > 64)
        throw new IllegalArgumentException("Nombre de dungeon inválido");
      points = List.copyOf(points);
      if (points.size() > 32 || points.stream().map(Arrival::id).distinct().count() != points.size())
        throw new IllegalArgumentException("Hasta 32 puntos distintos por dungeon");
    }
  }
  public record Data(int schema, Map<String, Dungeon> dungeons) {
    public Data {
      if (schema != 1 || dungeons == null || dungeons.size() > 64)
        throw new IllegalArgumentException("Entradas de dungeon inválidas");
      dungeons = Map.copyOf(dungeons);
      dungeons.forEach((id, dungeon) -> { if (!id.equals(dungeon.id())) throw new IllegalArgumentException("ID incorrecto"); });
    }
  }
  private final Path path;
  private Data data;
  public DungeonEntrances(Path path) throws IOException {
    this.path = path;
    data = AtomicJson.read(path, Data.class, new Data(1, Map.of()));
    if (data == null) throw new IOException("Entradas vacías; registro conservado");
  }
  public Map<String, Dungeon> all() { return data.dungeons(); }
  public Dungeon get(String id) {
    var dungeon = all().get(id);
    if (dungeon == null) throw new IllegalArgumentException("Dungeon de entrada inexistente");
    return dungeon;
  }
  public void save(String id, String name) throws IOException {
    put(new Dungeon(id, name, all().containsKey(id) ? get(id).points() : List.of()));
  }
  public void put(Dungeon dungeon) throws IOException {
    var next = new LinkedHashMap<>(all()); next.put(dungeon.id(), dungeon); write(next);
  }
  public void capture(String id, String name, Arrival point, boolean replace) throws IOException {
    var points = new ArrayList<>(get(id).points());
    if (replace && points.stream().noneMatch(p -> p.id().equals(point.id())))
      throw new IllegalArgumentException("Selecciona un punto existente");
    points.removeIf(p -> p.id().equals(point.id())); points.add(point);
    put(new Dungeon(id, name, points));
  }
  public void removePoint(String id, String point) throws IOException {
    var dungeon = get(id); var next = new ArrayList<>(dungeon.points());
    if (!next.removeIf(p -> p.id().equals(point))) throw new IllegalArgumentException("Punto inexistente");
    put(new Dungeon(id, dungeon.name(), next));
  }
  public void remove(String id) throws IOException {
    get(id); var next = new LinkedHashMap<>(all()); next.remove(id); write(next);
  }
  private void write(Map<String, Dungeon> next) throws IOException {
    var value = new Data(1, next); AtomicJson.write(path, value); data = value;
  }
  public static String target(String command) {
    if (!command.startsWith(PREFIX)) return "";
    String id = command.substring(PREFIX.length()); Definitions.id(id); return id;
  }
}
