package com.ianblk.zianmanager.client;
import java.util.*;
/** Semantic groups shared by presentation and coverage checks. */
public final class EditorSections {
 private EditorSections(){}
 public static Map<String,List<String>> groups(String type){var m=new LinkedHashMap<String,List<String>>();switch(type){
  case "mob"->{m.put("Básico",List.of("name","entity","boss","loot"));m.put("Combate",List.of("health","damage","armor","toughness","knockback"));m.put("Equipo",List.of("slot"));m.put("Efectos",List.of());m.put("Avanzado",List.of("id","effects"));}
  case "loot"->{m.put("Tabla",List.of("id","category","rolls"));m.put("Objetos",List.of("weight","min","max"));m.put("Avanzado",List.of("table"));}
  case "zone"->{m.put("General",List.of("id","name","team","enabled"));m.put("Ubicación",List.of());m.put("Tamaño",List.of("radiusX","radiusZ","above","below"));m.put("Oleadas",List.of("waves","pauseSeconds","respawnSeconds","completionLoot"));}
  case "npc"->{m.put("Apariencia",List.of("name","skin"));m.put("Diálogo",List.of("text"));m.put("Acción",List.of("button","command","cooldownSeconds"));}
  case "chest"->m.put("Cofre",List.of("loot","noCooldown","minutes"));
 }return Collections.unmodifiableMap(m);}
 public static String title(String type){return switch(type){case "root"->"Zian Manager";case "mobs"->"Mobs y jefes";case "lootlist"->"Tablas de recompensas";case "zones"->"Zonas de dungeon";case "npcs"->"Personajes y diálogos";case "chests"->"Cofres de dungeon";case "mob"->"Editar mob o jefe";case "loot"->"Editar recompensas";case "zone"->"Editar zona";case "npc"->"Editar personaje";case "chest"->"Configurar cofre";case "claim"->"Tus recompensas";default->"Zian Manager";};}
}
