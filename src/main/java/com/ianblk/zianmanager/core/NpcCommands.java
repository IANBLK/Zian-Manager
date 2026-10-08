package com.ianblk.zianmanager.core;
import java.nio.file.Path;
import java.io.IOException;
import java.util.*;
/** Persist intent before running a configured command; ambiguous executions never replay automatically. */
public final class NpcCommands {
    public record Entry(String state,long at){}
    public record Data(int schema,Map<String,Entry> entries){public Data{if(schema!=1 || entries.size()>100000)throw new IllegalArgumentException("Diario NPC inválido");entries=Map.copyOf(entries);}}
    private final Path path;private Data data;
    public NpcCommands(Path path)throws IOException{this.path=path;data=AtomicJson.read(path,Data.class,new Data(1,Map.of()));}
    private String key(UUID npc,UUID player){return npc+":"+player;}
    public void begin(UUID npc,UUID player,int cooldown,long now)throws IOException{
        var old=data.entries().get(key(npc,player));if(old!=null){if(!old.state().equals("DONE"))throw new IllegalArgumentException("Acción NPC pendiente de revisión; no se repetirá.");if(now-old.at()<cooldown*1000L)throw new IllegalArgumentException("Espera antes de volver a usar este NPC.");}put(npc,player,new Entry("STARTED",now));
    }
    public void complete(UUID npc,UUID player,boolean success,long now)throws IOException{put(npc,player,new Entry(success?"DONE":"FAILED",now));}
    public void reset(UUID npc)throws IOException{var map=new HashMap<>(data.entries());map.keySet().removeIf(k->k.startsWith(npc+":"));var next=new Data(1,map);AtomicJson.write(path,next);data=next;}
    private void put(UUID npc,UUID player,Entry entry)throws IOException{var map=new HashMap<>(data.entries());map.put(key(npc,player),entry);var next=new Data(1,map);AtomicJson.write(path,next);data=next;}
}
