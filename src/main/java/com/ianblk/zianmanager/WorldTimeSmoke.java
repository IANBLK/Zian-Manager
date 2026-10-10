package com.ianblk.zianmanager;
import com.ianblk.zianmanager.core.*;
import java.nio.file.*;
import java.time.*;
import java.util.*;
import net.minecraft.server.level.ServerPlayer;
public final class WorldTimeSmoke {
 public static void verify(ManagerRuntime runtime,ServerPlayer p)throws Exception{
  var server=p.getServer();var base=server.getWorldPath(net.minecraft.world.level.storage.LevelResource.ROOT).resolve("data/zianmanager/smoke-world-time-"+UUID.randomUUID());
  var dungeon=new DungeonTimeRuntime(server,base,"dungeon",DailyDungeonTime.Settings.defaults(),true);var times=new WorldTimeRuntime(server,base,()->dungeon);
  for(String id:List.of("nether","end","farmeo")){var json=AtomicJson.GSON.toJsonTree(times.profile(id).settings()).getAsJsonObject();json.addProperty("enabled",true);if(id.equals("farmeo")){var worlds=new com.google.gson.JsonArray();worlds.add("minecraft:overworld");json.add("worlds",worlds);}times.configure(id,AtomicJson.GSON.fromJson(json,DailyDungeonTime.Settings.class));}
  var day=times.profile("nether").settings().day(Instant.now());var field=DungeonTimeRuntime.class.getDeclaredField("usage");field.setAccessible(true);var nether=(DailyDungeonTime)field.get(times.profile("nether"));nether.charge(Map.of(p.getUUID(),10800L),day);
  if(times.profile("nether").remaining(p)!=0 || times.profile("end").remaining(p)!=10800 || times.profile("farmeo").remaining(p)!=10800 || dungeon.remaining(p)!=dungeon.limit(p,Instant.now()))throw new IllegalStateException("Time pool consumption leaked");
  if(!times.denyTravel(p,net.minecraft.world.level.Level.NETHER) || times.denyTravel(p,net.minecraft.world.level.Level.END))throw new IllegalStateException("Independent destination guard failed");
  server.getPlayerList().op(p.getGameProfile());try{if(times.denyTravel(p,net.minecraft.world.level.Level.NETHER))throw new IllegalStateException("OP blocked by world limit");}finally{server.getPlayerList().deop(p.getGameProfile());}
  // A depleted current world must keep penalties while unrelated budgets tick or receive bonuses.
  var farming=(DailyDungeonTime)field.get(times.profile("farmeo"));farming.charge(Map.of(p.getUUID(),10800L),day);TimePenalty.apply(p);times.profile("nether").tick();times.profile("end").bonus(p.getUUID(),1);
  if(!p.hasEffect(net.minecraft.world.effect.MobEffects.DARKNESS))throw new IllegalStateException("Unrelated world cleared active penalties");
  times.profile("farmeo").bonus(p.getUUID(),1);times.refresh();if(p.hasEffect(net.minecraft.world.effect.MobEffects.DARKNESS))throw new IllegalStateException("Current-world bonus did not clear penalties");
  if(times.profile("end").remaining(p)!=10860 || times.profile("nether").remaining(p)!=0 || times.profile("farmeo").remaining(p)!=60)throw new IllegalStateException("World bonus leaked");
  var duplicate=AtomicJson.GSON.toJsonTree(times.profile("end").settings()).getAsJsonObject();duplicate.add("worlds",AtomicJson.GSON.toJsonTree(List.of("minecraft:the_nether")));try{times.configure("end",AtomicJson.GSON.fromJson(duplicate,DailyDungeonTime.Settings.class));throw new IllegalStateException("Overlapping time worlds accepted");}catch(IllegalArgumentException expected){}
  var restored=new WorldTimeRuntime(server,base,()->dungeon);if(restored.profile("nether").remaining(p)!=0 || restored.profile("farmeo").remaining(p)!=60)throw new IllegalStateException("Independent balances did not survive restart");
  // Verify first-charge and logout behavior without waiting for wall time.
  times.profile("end").logout(p.getUUID());times.logout(p.getUUID());runtime.worldTime().refresh();
  ZianManager.LOGGER.info("Zian Manager independent world time smoke passed: separate consumption and bonuses, persisted restart, destination guards, OP bypass, no cross-profile penalty clearing, overlapping worlds rejected");
 }
}
