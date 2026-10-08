package com.ianblk.zianmanager.core;
import java.util.*;

public final class Definitions {
    private Definitions(){}
    public static void id(String id){if(id==null || !id.matches("[a-z0-9_]{1,32}"))throw new IllegalArgumentException("ID: usa 1–32 letras minúsculas, números o _");}
    public static void resource(String id){if(id==null || !id.matches("[a-z0-9_.-]+:[a-z0-9_./-]+"))throw new IllegalArgumentException("Identificador inválido: "+id);}
    public record Point(double x,double y,double z,float yaw){public Point{if(!Double.isFinite(x+y+z+yaw) || Math.abs(x)>29999984 || Math.abs(z)>29999984 || y< -64 || y>320)throw new IllegalArgumentException("Posición fuera de límites");}}
    public record Effect(String id,int amplifier,int seconds){public Effect{resource(id);if(amplifier<0 || amplifier>10 || seconds<1 || seconds>86400)throw new IllegalArgumentException("Efecto inválido");}}
    public record MobSpec(String id,String name,String entity,double health,double damage,double armor,double toughness,double knockback,List<Effect> effects,Map<String,String> equipment,String loot,boolean boss){
        public MobSpec{Definitions.id(id);resource(entity);if(name==null || name.isBlank() || name.length()>80)throw new IllegalArgumentException("Nombre inválido");
            if(!Double.isFinite(health+damage+armor+toughness+knockback) || health<1 || health>1024 || damage<0 || damage>128 || armor<0 || armor>30 || toughness<0 || toughness>20 || knockback<0 || knockback>1)throw new IllegalArgumentException("Atributos fuera de rango");
            effects=List.copyOf(effects);equipment=Map.copyOf(equipment);if(effects.size()>8 || equipment.size()>6 || equipment.keySet().stream().anyMatch(s->!Set.of("mainhand","offhand","head","chest","legs","feet").contains(s)))throw new IllegalArgumentException("Equipo o efectos inválidos");
            if(loot==null)loot="";if(!loot.isEmpty())Definitions.id(loot);
        }
        public MobSpec equip(String slot,String item){var next=new LinkedHashMap<>(equipment);next.put(slot,item);return new MobSpec(id,name,entity,health,damage,armor,toughness,knockback,effects,next,loot,boss);}
    }
    public record LootEntry(String item,int weight,int min,int max){public LootEntry{if(item==null || item.isBlank() || item.length()>65536 || weight<1 || weight>10000 || min<1 || max<min || max>64)throw new IllegalArgumentException("Entrada de loot inválida");}}
    public record LootSpec(String id,String category,int rolls,List<LootEntry> entries,String table){public LootSpec{Definitions.id(id);if(!Set.of("MOB","BOSS","CHEST").contains(category))throw new IllegalArgumentException("Categoría inválida");entries=List.copyOf(entries);table=table==null?"":table;if(!table.isEmpty())resource(table);if(rolls<1 || rolls>32 || entries.size()>64)throw new IllegalArgumentException("Loot fuera de límites");if(entries.isEmpty() && table.isEmpty())throw new IllegalArgumentException("Añade objetos o selecciona una loot table");}}
    public record ZoneSpec(String id,String dimension,Point first,Point second,List<Point> points,List<String> team,int waves,int pauseSeconds,int respawnSeconds,String completionLoot,boolean enabled){
        public ZoneSpec{Definitions.id(id);resource(dimension);points=List.copyOf(points);team=List.copyOf(team);team.forEach(Definitions::id);completionLoot=completionLoot==null?"":completionLoot;if(!completionLoot.isEmpty())Definitions.id(completionLoot);
            if(waves<1 || waves>16 || pauseSeconds<1 || pauseSeconds>3600 || respawnSeconds<10 || respawnSeconds>2592000 || points.size()>16 || team.size()>6)throw new IllegalArgumentException("Zona fuera de límites");
            if(first!=null && second!=null && (Math.abs(first.x-second.x)>128 || Math.abs(first.z-second.z)>128 || Math.abs(first.y-second.y)>64))throw new IllegalArgumentException("Zona máxima: 128×64×128");
            var unique=new HashSet<String>();for(var p:points)if(!unique.add(((int)Math.floor(p.x))+":"+((int)Math.floor(p.y))+":"+((int)Math.floor(p.z))))throw new IllegalArgumentException("Cada punto debe estar en un bloque distinto");
            if(enabled && first!=null && second!=null)for(var p:points)if(p.x<Math.min(first.x,second.x) || p.x>=Math.max(first.x,second.x)+1 || p.z<Math.min(first.z,second.z) || p.z>=Math.max(first.z,second.z)+1 || p.y<Math.min(first.y,second.y) || p.y>=Math.max(first.y,second.y)+2)throw new IllegalArgumentException("Los puntos deben estar dentro de la zona");
            if(enabled && (first==null || second==null || team.size()<3 || points.size()<team.size()))throw new IllegalArgumentException("Configura las dos esquinas y un punto por cada uno de los 3–6 mobs");
        }
        public boolean contains(String dimension,double x,double y,double z){return this.dimension.equals(dimension) && first!=null && second!=null && x>=Math.min(first.x,second.x) && x<Math.max(first.x,second.x)+1 && y>=Math.min(first.y,second.y) && y<Math.max(first.y,second.y)+2 && z>=Math.min(first.z,second.z) && z<Math.max(first.z,second.z)+1;}
    }
    public record ChestSpec(UUID uuid,String dimension,int x,int y,int z,String loot,int minutes,String block,String facing){public ChestSpec(UUID uuid,String dimension,int x,int y,int z,String loot,int minutes){this(uuid,dimension,x,y,z,loot,minutes,"zianmanager:dungeon_chest","north");}public ChestSpec{block=block==null?"zianmanager:dungeon_chest":block;resource(block);facing=facing==null?"north":facing;if(!Set.of("north","south","east","west").contains(facing))throw new IllegalArgumentException("Orientación inválida");Objects.requireNonNull(uuid);resource(dimension);Definitions.id(loot);if(minutes<1 || minutes>43200)throw new IllegalArgumentException("Espera: 1–43200 minutos");}}
    public static final List<String> NPC_SKINS=List.of("heraldo_real","guardian_celeste","centinela_sombrio","caballero_infernal","guardian_abisal","maga_prismatica","paladin_dorado","explorador_bronce","guardiana_celestial","hechicera_aurora");
    public record NpcSpec(UUID uuid,String dimension,Point point,String name,String text,String skin,String button,String command,int cooldownSeconds){
        public NpcSpec(UUID uuid,String dimension,Point point,String name,String text){this(uuid,dimension,point,name,text,"heraldo_real","Continuar","",60);}
        public NpcSpec{Objects.requireNonNull(uuid);Objects.requireNonNull(point);resource(dimension);if(name==null || name.isBlank() || name.length()>80 || text==null || text.isBlank() || text.length()>1024)throw new IllegalArgumentException("Diálogo inválido");
            skin=skin==null || skin.isBlank()?"heraldo_real":skin;button=button==null || button.isBlank()?"Continuar":button;command=command==null?"":command.trim();
            if(!NPC_SKINS.contains(skin) || button.length()>64 || command.length()>2048 || command.chars().anyMatch(Character::isISOControl) || cooldownSeconds<0 || cooldownSeconds>2592000)throw new IllegalArgumentException("Acción o skin inválida");
            if(command.startsWith("/"))command=command.substring(1);
        }
    }
}
