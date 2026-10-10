package com.ianblk.zianmanager;

import com.ianblk.zianmanager.core.DailyDungeonTime;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.network.chat.Component;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Supplier;

/** Coordinates independent persisted allowances, one active HUD and one penalty owner. */
public final class WorldTimeRuntime {
 private final MinecraftServer server;
 private final Supplier<DungeonTimeRuntime> dungeon;
 private final Map<String,DungeonTimeRuntime> farming=new LinkedHashMap<>();
 public WorldTimeRuntime(MinecraftServer server,Path base,Supplier<DungeonTimeRuntime> dungeon)throws java.io.IOException{
  this.server=server;this.dungeon=dungeon;
  for(String id:List.of("nether","end","farmeo"))farming.put(id,new DungeonTimeRuntime(server,base,id,com.ianblk.zianmanager.core.WorldTimeDefaults.farming(id),true));
  for(var time:profiles().values())time.limitedDestination(this::limitedWorld);
  validate(null,null);
 }
 public Map<String,DungeonTimeRuntime> profiles(){var all=new LinkedHashMap<String,DungeonTimeRuntime>();all.put("dungeon",dungeon.get());all.putAll(farming);return Collections.unmodifiableMap(all);}
 public DungeonTimeRuntime profile(String id){var value=profiles().get(id.isBlank()?"dungeon":id);if(value==null)throw new IllegalArgumentException("Perfil de tiempo desconocido");return value;}
 public boolean allowed(ServerPlayer p){return profiles().values().stream().allMatch(t->t.allowed(p));}
 public boolean limitedWorld(ServerLevel level){return profiles().values().stream().anyMatch(t->t.settings().enabled() && t.limitedWorld(level));}
 private Set<String> worldKeys(DailyDungeonTime.Settings settings){var keys=new HashSet<>(settings.worlds());for(var level:server.getAllLevels())if(settings.worlds().contains(level.dimension().location().toString()))keys.add(level.dimension().location().toString());else try{var world=level.getClass().getMethod("getWorld").invoke(level);String name=(String)Class.forName("org.bukkit.World").getMethod("getName").invoke(world);if(settings.worlds().contains(name))keys.add(level.dimension().location().toString());}catch(Exception|LinkageError ignored){}return keys;}
 private void validate(String changed,DailyDungeonTime.Settings replacement){var seen=new HashSet<String>();for(var e:profiles().entrySet()){var settings=e.getKey().equals(changed)?replacement:e.getValue().settings();if(!settings.enabled())continue;for(String key:worldKeys(settings))if(!seen.add(key))throw new IllegalArgumentException("Un mundo no puede pertenecer a dos perfiles de tiempo: "+key);}}
 public void configure(String id,DailyDungeonTime.Settings settings)throws java.io.IOException{profile(id);validate(id.isBlank()?"dungeon":id,settings);profile(id).configure(settings);refresh();}
 public void tick(){for(var e:profiles().entrySet())try{e.getValue().tick();}catch(Exception error){ZianManager.LOGGER.error("Time profile tick failed: {}",e.getKey(),error);}refresh();}
 private void present(ServerPlayer p){if(allowed(p))TimePenalty.clear(p);var active=profiles().values().stream().filter(t->t.inside(p)).findFirst();if(active.isPresent())active.get().sendTimer(p);else ManagerNetwork.sendTimer(p,new ManagerNetwork.Timer(false,false,0,0,0,""));}
 public void refresh(){for(var p:server.getPlayerList().getPlayers())present(p);}
 public boolean denyTravel(ServerPlayer p,net.minecraft.resources.ResourceKey<net.minecraft.world.level.Level> target){return profiles().values().stream().anyMatch(t->t.denyTravel(p,target));}
 public void changedDimension(ServerPlayer p){for(var t:profiles().values())t.changedDimension(p);present(p);}
 public void logout(UUID id){profiles().values().forEach(t->t.logout(id));}
 public void describe(ServerPlayer p,String id){var selected=id==null?profiles():Map.of(id,profile(id));for(var e:selected.entrySet()){var t=e.getValue();p.sendSystemMessage(Component.literal(e.getKey()+": "+(!t.settings().enabled()?"sin límite configurado":t.unlimited(p)?"ilimitado":ZoneHolograms.countdown(t.remaining(p)*1000))));}}
}
