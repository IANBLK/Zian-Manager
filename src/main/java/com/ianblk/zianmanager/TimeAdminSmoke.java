package com.ianblk.zianmanager;
import com.ianblk.zianmanager.core.*;
import net.minecraft.server.level.ServerPlayer;
import java.time.Instant;
import java.util.*;
/** Isolated native administrative command and permission verification. */
public final class TimeAdminSmoke {
 public static void verify(ManagerRuntime runtime,ServerPlayer player)throws Exception{
  var server=player.getServer();var original=runtime.dungeonTime();var field=ManagerRuntime.class.getDeclaredField("dungeonTime");field.setAccessible(true);
  var base=server.getWorldPath(net.minecraft.world.level.storage.LevelResource.ROOT).resolve("data/zianmanager/smoke-admin-time-"+UUID.randomUUID());var time=new DungeonTimeRuntime(server,base);
  var settings=new DailyDungeonTime.Settings(true,List.of("minecraft:overworld"),"America/Guayaquil",1,1,2,2,"zianmanager.dungeon.vip","zianmanager.dungeon.bypass","execute in minecraft:the_nether run tp {player} 2048 201 2048");time.configure(settings);field.set(runtime,time);
  var commands=server.getCommands().getDispatcher();String command="zianmanager removetime "+player.getGameProfile().getName()+" 90";var level=player.serverLevel();var position=player.position();
  try{
   if(commands.getRoot().getChild("zianmanager").getChild("removetime").canUse(player.createCommandSourceStack()))throw new IllegalStateException("Non-admin sees removal command");
   boolean rejected=false;try{commands.execute(command,player.createCommandSourceStack());}catch(com.mojang.brigadier.exceptions.CommandSyntaxException expected){rejected=true;}if(!rejected || time.remaining(player)!=60)throw new IllegalStateException("Non-admin modified time");
   server.getPlayerList().op(player.getGameProfile());try{if(commands.execute(command,server.createCommandSourceStack())!=0 || time.remaining(player)!=60)throw new IllegalStateException("Unlimited player was debited");}finally{server.getPlayerList().deop(player.getGameProfile());}
   var spawn=server.overworld().getSharedSpawnPos();player.teleportTo(server.overworld(),spawn.getX(),spawn.getY()+2,spawn.getZ(),Set.of(),0,0);
   if(commands.execute(command,server.createCommandSourceStack())!=1 || time.remaining(player)!=0 || player.serverLevel()==server.overworld())throw new IllegalStateException("Removal did not clamp or immediately eject depleted player");
   var stored=new DailyDungeonTime(base.resolve("dungeon-time-usage.json"));if(stored.used(player.getUUID(),settings.day(Instant.now()))!=60)throw new IllegalStateException("Administrative deduction did not persist");
   commands.execute("zianmanager bonustime player "+player.getGameProfile().getName()+" 2",server.createCommandSourceStack());if(time.remaining(player)!=120)throw new IllegalStateException("Bonus could not restore depleted balance");
   commands.execute("zianmanager removetime "+player.getGameProfile().getName()+" 1",server.createCommandSourceStack());if(time.remaining(player)!=60)throw new IllegalStateException("Partial removal did not preserve remaining bonus");
   ZianManager.LOGGER.info("Zian Manager administrative time smoke passed: permission denial, unlimited refusal, clamping, immediate exit, persisted consumption and bonus restoration");
  }finally{field.set(runtime,original);player.teleportTo(level,position.x,position.y,position.z,Set.of(),0,0);}
 }
}
