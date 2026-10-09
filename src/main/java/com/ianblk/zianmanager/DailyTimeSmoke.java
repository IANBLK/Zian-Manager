package com.ianblk.zianmanager;
import com.ianblk.zianmanager.core.*;
import java.time.Instant;
import java.util.*;
import net.minecraft.server.level.ServerPlayer;
public final class DailyTimeSmoke {
 public static void verify(ManagerRuntime runtime,ServerPlayer first)throws Exception{var original=runtime.dungeonTime();var server=first.getServer();var base=server.getWorldPath(net.minecraft.world.level.storage.LevelResource.ROOT).resolve("data/zianmanager/smoke-time-"+UUID.randomUUID());var time=new DungeonTimeRuntime(server,base);var runtimeField=ManagerRuntime.class.getDeclaredField("dungeonTime");runtimeField.setAccessible(true);runtimeField.set(runtime,time);var previous=time.settings();var file=base.resolve("dungeon-time-usage.json");var settings=new DailyDungeonTime.Settings(true,List.of("minecraft:overworld"),"America/Guayaquil",1,1,2,2,"zianmanager.dungeon.vip","zianmanager.dungeon.bypass","execute in minecraft:the_nether run tp {player} 2048 201 2048");time.configure(settings);var field=DungeonTimeRuntime.class.getDeclaredField("usage");field.setAccessible(true);var usage=(DailyDungeonTime)field.get(time);var day=settings.day(Instant.now());usage.charge(Map.of(first.getUUID(),60L),day);server.getPlayerList().op(first.getGameProfile());try{time.tick();if(!time.unlimited(first) || !first.serverLevel().dimension().equals(net.minecraft.world.level.Level.OVERWORLD))throw new IllegalStateException("OP was charged or ejected");server.getCommands().getDispatcher().execute("zianmanager bonustime player "+first.getGameProfile().getName()+" 30",first.createCommandSourceStack());if(time.remaining(first)!=1800)throw new IllegalStateException("Personal bonus command did not add 30 minutes");server.getCommands().getDispatcher().execute("zianmanager bonustime all 30",first.createCommandSourceStack());if(time.remaining(first)!=3600)throw new IllegalStateException("Global bonus command did not add 30 minutes");}finally{server.getPlayerList().deop(first.getGameProfile());}usage.charge(Map.of(first.getUUID(),3600L),day);if(time.remaining(first)!=0)throw new IllegalStateException("Daily exhaustion not recognized");time.tick();if(!first.serverLevel().dimension().equals(net.minecraft.world.level.Level.NETHER))throw new IllegalStateException("Exhausted player not sent using console exit command");var overworld=server.overworld();var spawn=overworld.getSharedSpawnPos();first.teleportTo(overworld,spawn.getX(),spawn.getY()+2,spawn.getZ(),Set.of(),0,0);time.tick();if(!first.serverLevel().dimension().equals(net.minecraft.world.level.Level.NETHER))throw new IllegalStateException("Reentry did not enforce depleted daily time");verifyNative(time,first);time.configure(previous);runtimeField.set(runtime,original);first.teleportTo(overworld,spawn.getX(),spawn.getY()+2,spawn.getZ(),Set.of(),0,0);if(new DailyDungeonTime(file).used(first.getUUID(),day)!=3660)throw new IllegalStateException("Daily time not persisted");ZianManager.LOGGER.info("Zian Manager daily time smoke passed: OP bypass, personal/global bonuses, exhaustion console teleport, rejected reentry and persisted usage");}
 private static void verifyNative(DungeonTimeRuntime time,ServerPlayer player)throws Exception{
  var server=player.getServer();var world=server.overworld();
  // FakePlayer's default packet listener intentionally ignores teleport coordinates.
  // Use the vanilla teleport implementation with packet delivery suppressed in this fixture.
  var oldConnection=player.connection;
  player.connection=new net.minecraft.server.network.ServerGamePacketListenerImpl(server,oldConnection.getConnection(),player,net.minecraft.server.network.CommonListenerCookie.createInitial(player.getGameProfile(),false)){
   @Override public void send(net.minecraft.network.protocol.Packet<?> packet){}
   @Override public void send(net.minecraft.network.protocol.Packet<?> packet,net.minecraft.network.PacketSendListener listener){}
   @Override public void tick(){}
  };
  var oldPos=player.getRespawnPosition();var oldDimension=player.getRespawnDimension();float oldAngle=player.getRespawnAngle();boolean oldForced=player.isRespawnForced();
  time.configure(new DailyDungeonTime.Settings(true,List.of("minecraft:the_nether"),"America/Guayaquil",1,1,2,2,"zianmanager.dungeon.vip","zianmanager.dungeon.bypass","bed"));
  var foot=new net.minecraft.core.BlockPos(2110,201,2110);var head=foot.south();
  try{
   world.getChunkAt(foot);
   for(int x=-3;x<=3;x++)for(int z=-3;z<=3;z++){
    var floor=foot.offset(x,-1,z);world.setBlockAndUpdate(floor,net.minecraft.world.level.block.Blocks.STONE.defaultBlockState());
    world.setBlockAndUpdate(floor.above(),net.minecraft.world.level.block.Blocks.AIR.defaultBlockState());world.setBlockAndUpdate(floor.above(2),net.minecraft.world.level.block.Blocks.AIR.defaultBlockState());
   }
   var bed=net.minecraft.world.level.block.Blocks.RED_BED.defaultBlockState().setValue(net.minecraft.world.level.block.BedBlock.FACING,net.minecraft.core.Direction.SOUTH);
   world.setBlock(foot,bed.setValue(net.minecraft.world.level.block.BedBlock.PART,net.minecraft.world.level.block.state.properties.BedPart.FOOT),2);
   world.setBlock(head,bed.setValue(net.minecraft.world.level.block.BedBlock.PART,net.minecraft.world.level.block.state.properties.BedPart.HEAD),2);
   player.setRespawnPosition(world.dimension(),head,0,false,false);
   if(!time.nativeExit(player,true) || player.position().distanceTo(net.minecraft.world.phys.Vec3.atCenterOf(head))>4)throw new IllegalStateException("Valid bed exit failed: bed="+world.getBlockState(head)+", respawn="+player.getRespawnPosition()+", player="+player.position()+", stand="+net.minecraft.world.level.block.BedBlock.findStandUpPosition(net.minecraft.world.entity.EntityType.PLAYER,world,head,net.minecraft.core.Direction.SOUTH,0)+", floor="+world.getBlockState(head.east().below())+", dim="+player.getRespawnDimension()+", beds="+net.minecraft.world.level.block.BedBlock.canSetSpawn(world));
   if(!time.nativeExit(player,false) || player.position().distanceTo(net.minecraft.world.phys.Vec3.atCenterOf(head))<100)throw new IllegalStateException("World spawn mode used the bed");
   world.setBlockAndUpdate(head,net.minecraft.world.level.block.Blocks.AIR.defaultBlockState());world.setBlockAndUpdate(foot,net.minecraft.world.level.block.Blocks.AIR.defaultBlockState());
   if(!time.nativeExit(player,true) || player.position().distanceTo(net.minecraft.world.phys.Vec3.atCenterOf(head))<100)throw new IllegalStateException("Missing bed did not fall back to world spawn");
   // An unavailable plugin command must also work on a pure NeoForge server.
   time.configure(new DailyDungeonTime.Settings(true,List.of("minecraft:the_nether"),"America/Guayaquil",1,1,2,2,"zianmanager.dungeon.vip","zianmanager.dungeon.bypass","zian_missing_spawn {player}"));
   var nether=server.getLevel(net.minecraft.world.level.Level.NETHER);
   player.teleportTo(nether,2048,201,2048,Set.of(),0,0);time.enforceEntry(player);
   if(player.serverLevel()!=world)throw new IllegalStateException("Missing plugin exit command left exhausted player in dungeon");
   ZianManager.LOGGER.info("Zian Manager native exit smoke passed: valid bed, world spawn, broken bed fallback and absent plugin command");
  }finally{player.connection=oldConnection;player.setRespawnPosition(oldDimension,oldPos,oldAngle,oldForced,false);}
 }
}
