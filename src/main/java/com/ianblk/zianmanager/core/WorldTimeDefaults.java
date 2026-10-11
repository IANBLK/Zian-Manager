package com.ianblk.zianmanager.core;
import java.util.*;
public final class WorldTimeDefaults {
 private WorldTimeDefaults(){}
 public static DailyDungeonTime.Settings farming(String id){String world=switch(id){case "nether"->"minecraft:the_nether";case "end"->"minecraft:the_end";case "farmeo"->"farmeo";default->throw new IllegalArgumentException("Perfil desconocido");};return new DailyDungeonTime.Settings(false,List.of(world),"America/Guayaquil",180,180,240,240,"zianmanager.farming.rank","zianmanager.farming.bypass","worldspawn",List.of(),"¡El tiempo diario de "+id+" se ha regenerado!","Se te acabó el tiempo de "+id+" por hoy. Se renovará a las 00:00 UTC -5.",100,100,true,"["+id+"] Te quedan {time} para hoy.","TIEMPO DE "+id.toUpperCase(Locale.ROOT),Map.of());}
}
