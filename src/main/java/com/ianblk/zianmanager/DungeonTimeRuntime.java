package com.ianblk.zianmanager;
import com.ianblk.zianmanager.core.*;
import com.ianblk.zianmanager.core.DailyDungeonTime.Settings;
import com.ianblk.zianmanager.permission.LuckPermsLookup;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import java.nio.file.*;
import java.time.*;
import java.util.*;
/** Bukkit bridge uses the player's actual world name and console spawn command on hybrid servers. */
public final class DungeonTimeRuntime {
 private final MinecraftServer server;private final Path settingsFile;private final DailyDungeonTime usage;private Settings settings;
 private record Session(String dimension,Instant at){}
 private final Set<UUID> ejecting=new HashSet<>();private final Map<UUID,LocalDate> notified=new HashMap<>();private boolean healthy=true;private final Map<UUID,Session> sessions=new HashMap<>();private final Map<UUID,Long> retry=new HashMap<>();
 public DungeonTimeRuntime(MinecraftServer server,Path base)throws java.io.IOException{this.server=server;settingsFile=base.resolve("dungeon-time.json");settings=AtomicJson.read(settingsFile,Settings.class,Settings.defaults());usage=new DailyDungeonTime(base.resolve("dungeon-time-usage.json"));}
 public Settings settings(){return settings;}
 public void configure(Settings value)throws java.io.IOException{AtomicJson.write(settingsFile,value);settings=value;}
 private static boolean permission(ServerPlayer player,String node){try{return LuckPermsLookup.lookup(player,node)==LuckPermsLookup.Decision.TRUE;}catch(Exception | LinkageError error){return false;}}
 public static String worldName(ServerPlayer player){try{var bukkit=Class.forName("org.bukkit.Bukkit");var bp=bukkit.getMethod("getPlayer",UUID.class).invoke(null,player.getUUID());if(bp==null)return "";var world=Class.forName("org.bukkit.entity.Entity").getMethod("getWorld").invoke(bp);return (String)Class.forName("org.bukkit.World").getMethod("getName").invoke(world);}catch(Exception | LinkageError ignored){return "";}}
 public boolean inside(ServerPlayer player){return settings.enabled() && (settings.worlds().contains(player.serverLevel().dimension().location().toString()) || settings.worlds().contains(worldName(player)));}
 public boolean unlimited(ServerPlayer player){return server.getPlayerList().isOp(player.getGameProfile()) || player.createCommandSourceStack().hasPermission(2) || permission(player,settings.bypassPermission());}
 public long limit(ServerPlayer player,Instant now){long value=settings.limit(now,false);if(permission(player,settings.vipPermission()))value=Math.max(value,settings.limit(now,true));for(var tier:settings.tiers())if(permission(player,tier.permission()))value=Math.max(value,tier.limit(settings.day(now)));return value;}
 public long remaining(ServerPlayer player){var now=Instant.now();return Math.max(0,limit(player,now)+usage.bonus(player.getUUID(),settings.day(now))-usage.used(player.getUUID(),settings.day(now)));}
 public void bonus(UUID player,int minutes)throws java.io.IOException{usage.grant(player,settings.day(Instant.now()),minutes*60L);retry.clear();notified.clear();}
 public void logout(UUID player){sessions.remove(player);retry.remove(player);notified.remove(player);}
 public boolean allowed(ServerPlayer player){return !inside(player) || unlimited(player) || healthy && remaining(player)>0;}
 public void tick()throws Exception{
  if(!settings.enabled()){sessions.clear();notified.clear();sendTimers();return;}var now=Instant.now();var day=settings.day(now);if(usage.announceDue(now,settings))try{usage.markAnnouncement(day);for(var player:server.getPlayerList().getPlayers())player.sendSystemMessage(Component.literal(settings.resetMessage()));}catch(java.io.IOException error){if(healthy)ZianManager.LOGGER.error("Daily reset announcement cannot be persisted",error);healthy=false;}var charges=new HashMap<UUID,Long>();var present=new HashSet<UUID>();var participants=new ArrayList<ServerPlayer>();
  for(var player:List.copyOf(server.getPlayerList().getPlayers())){if(!inside(player) || unlimited(player))continue;present.add(player.getUUID());participants.add(player);String dimension=player.serverLevel().dimension().location().toString();var previous=sessions.get(player.getUUID());var pulse=DailyDungeonTime.pulse(previous==null || !previous.dimension().equals(dimension)?null:previous.at(),now,day,ZoneId.of(settings.timezone()));sessions.put(player.getUUID(),new Session(dimension,pulse.at()));long elapsed=pulse.seconds();charges.put(player.getUUID(),Math.min(remaining(player),Math.min(elapsed,86400)));}
  sessions.keySet().retainAll(present);retry.keySet().retainAll(present);notified.keySet().retainAll(present);try{usage.charge(charges,day);}catch(java.io.IOException error){if(healthy)ZianManager.LOGGER.error("Daily dungeon usage cannot be saved; returning affected players to spawn",error);healthy=false;}
  for(var player:participants){long left=healthy?remaining(player):0;if(left>0){notified.remove(player.getUUID());retry.remove(player.getUUID());continue;}enforceEntry(player);}
  sendTimers();
 }
 private void sendTimers(){for(var player:server.getPlayerList().getPlayers())sendTimer(player);}
 private void sendTimer(ServerPlayer player){boolean visible=settings.hudEnabled() && inside(player);ManagerNetwork.sendTimer(player,new ManagerNetwork.Timer(visible,visible && unlimited(player),visible?remaining(player):0,settings.hudX(),settings.hudY()));}
 public void changedDimension(ServerPlayer player){if(!ejecting.contains(player.getUUID()))retry.remove(player.getUUID());enforceEntry(player);sendTimer(player);}
 public void enforceEntry(ServerPlayer player){if(!inside(player)){logout(player.getUUID());return;}if(ejecting.contains(player.getUUID()) || unlimited(player) || healthy && remaining(player)>0)return;
if(!settings.day(Instant.now()).equals(notified.put(player.getUUID(),settings.day(Instant.now()))))player.sendSystemMessage(Component.literal(healthy?settings.exhaustedMessage():"No se pudo guardar tu tiempo de dungeon. Regresando al spawn para conservar tu registro."));player.displayClientMessage(Component.literal("Tiempo diario de dungeon agotado. Regresando al spawn…"),true);long millis=System.currentTimeMillis();if(millis<retry.getOrDefault(player.getUUID(),0L))return;retry.put(player.getUUID(),millis+15000);String command=settings.exitCommand().replace("{player}",player.getGameProfile().getName()).replaceFirst("^/","");ejecting.add(player.getUUID());try{if(!exit(player,command))throw new IllegalStateException("El comando no confirmó la salida");}catch(Exception error){ZianManager.LOGGER.error("Dungeon time exit failed for {} using {}; retry in 15s",player.getUUID(),command,error);}finally{ejecting.remove(player.getUUID());}
 }
 private boolean exit(ServerPlayer player,String command)throws Exception{
  if(command.equals("bed") || command.equals("worldspawn"))return nativeExit(player,command.equals("bed"));
  try{
   boolean accepted;
   try{var bukkit=Class.forName("org.bukkit.Bukkit");var console=bukkit.getMethod("getConsoleSender").invoke(null);accepted=(Boolean)bukkit.getMethod("dispatchCommand",Class.forName("org.bukkit.command.CommandSender"),String.class).invoke(null,console,command);}
   catch(ClassNotFoundException pureNeoForge){accepted=server.getCommands().getDispatcher().execute(command,server.createCommandSourceStack().withPermission(4))>0;}
   if(accepted)return true;
  }catch(Exception | LinkageError error){ZianManager.LOGGER.warn("Dungeon exit command unavailable; using native bed/world spawn for {}",player.getUUID());}
  if(!inside(player))return true;
  return nativeExit(player,true);
 }
 /** Native respawn lookup does not kill the player or consume respawn-anchor charges. */
 boolean nativeExit(ServerPlayer player,boolean preferBed){
  var destination=new net.minecraft.world.level.portal.DimensionTransition(server.overworld(),player,net.minecraft.world.level.portal.DimensionTransition.DO_NOTHING);
  if(preferBed){
   var level=server.getLevel(player.getRespawnDimension());var pos=player.getRespawnPosition();
   if(level!=null && pos!=null && !limitedWorld(level) && level.getBlockState(pos).getBlock() instanceof net.minecraft.world.level.block.BedBlock && net.minecraft.world.level.block.BedBlock.canSetSpawn(level)){
    var state=level.getBlockState(pos);
    var stand=net.minecraft.world.level.block.BedBlock.findStandUpPosition(net.minecraft.world.entity.EntityType.PLAYER,level,pos,state.getValue(net.minecraft.world.level.block.BedBlock.FACING),player.getRespawnAngle());
    if(stand.isPresent())destination=new net.minecraft.world.level.portal.DimensionTransition(level,stand.get(),net.minecraft.world.phys.Vec3.ZERO,player.getRespawnAngle(),0,net.minecraft.world.level.portal.DimensionTransition.DO_NOTHING);
   }
  }
  if(limitedWorld(destination.newLevel()))throw new IllegalStateException("El spawn de salida pertenece a un mundo con tiempo limitado");
  player.stopRiding();var point=destination.pos();player.teleportTo(destination.newLevel(),point.x,point.y,point.z,Set.of(),destination.yRot(),destination.xRot());
  player.setDeltaMovement(net.minecraft.world.phys.Vec3.ZERO);player.fallDistance=0;
  return !inside(player);
 }
 private boolean limitedWorld(net.minecraft.server.level.ServerLevel level){
  if(settings.worlds().contains(level.dimension().location().toString()))return true;
  try{var craft=level.getClass().getMethod("getWorld").invoke(level);return settings.worlds().contains((String)Class.forName("org.bukkit.World").getMethod("getName").invoke(craft));}catch(Exception | LinkageError ignored){return false;}
 }
}
