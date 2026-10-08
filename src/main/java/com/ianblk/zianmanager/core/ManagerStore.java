package com.ianblk.zianmanager.core;
import com.ianblk.zianmanager.core.Definitions.*;
import java.nio.file.Path;
import java.io.IOException;
import java.util.*;
public final class ManagerStore {
    public record Data(int schema,Map<String,MobSpec> mobs,Map<String,LootSpec> loot,Map<String,ZoneSpec> zones,Map<UUID,ChestSpec> chests,Map<UUID,NpcSpec> npcs){
        public Data{if(schema!=1)throw new IllegalArgumentException("Versión desconocida");mobs=Map.copyOf(mobs);loot=Map.copyOf(loot);zones=Map.copyOf(zones);chests=Map.copyOf(chests);npcs=Map.copyOf(npcs);if(mobs.size()>128 || loot.size()>128 || zones.size()>64 || chests.size()>512 || npcs.size()>256)throw new IllegalArgumentException("Demasiadas definiciones");
            mobs.forEach((k,v)->{if(!k.equals(v.id()))throw new IllegalArgumentException("ID incorrecto");});loot.forEach((k,v)->{if(!k.equals(v.id()))throw new IllegalArgumentException("ID incorrecto");});zones.forEach((k,v)->{if(!k.equals(v.id()))throw new IllegalArgumentException("ID incorrecto");});
        }
    }
    private final Path path;private Data data;
    public ManagerStore(Path path) throws IOException{this.path=path;data=AtomicJson.read(path,Data.class,new Data(1,Map.of(),Map.of(),Map.of(),Map.of(),Map.of()));if(data==null)throw new IOException("Datos vacíos; archivo conservado");}
    public Data data(){return data;}
    public void put(Object value) throws IOException{
        var mobs=new LinkedHashMap<>(data.mobs);var loot=new LinkedHashMap<>(data.loot);var zones=new LinkedHashMap<>(data.zones);var chests=new LinkedHashMap<>(data.chests);var npcs=new LinkedHashMap<>(data.npcs);
        if(value instanceof MobSpec m)mobs.put(m.id(),m);else if(value instanceof LootSpec l)loot.put(l.id(),l);else if(value instanceof ZoneSpec z)zones.put(z.id(),z);else if(value instanceof ChestSpec c)chests.put(c.uuid(),c);else if(value instanceof NpcSpec n)npcs.put(n.uuid(),n);else throw new IllegalArgumentException("Tipo desconocido");
        var next=new Data(1,mobs,loot,zones,chests,npcs);AtomicJson.write(path,next);data=next;
    }
    public void remove(String type,String id) throws IOException{
        var mobs=new LinkedHashMap<>(data.mobs);var loot=new LinkedHashMap<>(data.loot);var zones=new LinkedHashMap<>(data.zones);var chests=new LinkedHashMap<>(data.chests);var npcs=new LinkedHashMap<>(data.npcs);
        switch(type){case "mob"->mobs.remove(id);case "loot"->loot.remove(id);case "zone"->zones.remove(id);case "chest"->chests.remove(UUID.fromString(id));case "npc"->npcs.remove(UUID.fromString(id));default->throw new IllegalArgumentException("Tipo desconocido");}
        var next=new Data(1,mobs,loot,zones,chests,npcs);AtomicJson.write(path,next);data=next;
    }
}
