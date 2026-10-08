package com.ianblk.zianmanager.core;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
public final class KeyLedger {
 public record Cost(UUID player,String key,String phase,String evidence){public Cost{Objects.requireNonNull(player);if(key==null || (!key.isBlank() && !key.matches("zianmanager:(common|rare|epic|legendary)_key")) || !Set.of("RESERVED","APPLYING","PAID","REVIEW").contains(phase))throw new IllegalArgumentException("Coste inválido");}}
 public record Data(int schema,Map<UUID,Cost> entries){public Data{if(schema!=1 || entries.size()>200000)throw new IllegalArgumentException("Diario inválido");entries=Map.copyOf(entries);}}
 public interface Port {boolean available(String key);boolean consumeAndSave(String key)throws Exception;}
 private final Path path;private Data data;
 public KeyLedger(Path path)throws IOException{this.path=path;data=AtomicJson.read(path,Data.class,new Data(1,Map.of()));}
 public Cost get(UUID claim){return data.entries().get(claim);}
 public Cost bind(UUID claim,UUID player,String key)throws IOException{var old=get(claim);if(old!=null){if(!old.player().equals(player))throw new IllegalArgumentException("Dueño incorrecto");return old;}var cost=new Cost(player,key,key.isEmpty()?"PAID":"RESERVED","");put(claim,cost);return cost;}
 public void take(UUID claim,UUID player,Port port)throws Exception{var c=get(claim);if(c==null)return;if(!c.player().equals(player))throw new IllegalArgumentException("Dueño incorrecto");if(c.phase().equals("PAID"))return;if(!c.phase().equals("RESERVED"))throw new IllegalArgumentException("Llave pendiente de revisión; no se consumirá otra.");if(!port.available(c.key()))throw new IllegalArgumentException("Necesitas la llave correspondiente en tu inventario.");put(claim,new Cost(player,c.key(),"APPLYING","consumption_started"));boolean confirmed=false;try{confirmed=port.consumeAndSave(c.key());}finally{put(claim,new Cost(player,c.key(),confirmed?"PAID":"REVIEW",confirmed?"inventory_readback":"unconfirmed"));}if(!confirmed)throw new IllegalArgumentException("Consumo de llave en revisión; loot conservado.");}
 public void resolve(UUID claim,UUID player,String evidence)throws IOException{var c=get(claim);if(c==null || !c.player().equals(player) || !Set.of("REVIEW","APPLYING").contains(c.phase()) || evidence==null || evidence.length()<8 || evidence.length()>500)throw new IllegalArgumentException("Revisión o evidencia inválida");put(claim,new Cost(player,c.key(),"PAID",evidence));}
 private void put(UUID claim,Cost cost)throws IOException{var map=new HashMap<>(data.entries());map.put(claim,cost);var next=new Data(1,map);AtomicJson.write(path,next);data=next;}
}
