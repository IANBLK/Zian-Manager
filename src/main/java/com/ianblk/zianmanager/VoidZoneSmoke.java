package com.ianblk.zianmanager;
import com.ianblk.zianmanager.core.Definitions.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import java.util.*;
/** Suspended platform in another dimension: no dependency on overworld terrain. */
public final class VoidZoneSmoke {
 public static void verify(ManagerRuntime runtime,ServerPlayer player)throws Exception{var level=player.getServer().getLevel(Level.NETHER);int x=2048,y=200,z=2048;for(int cx=(x-3)>>4;cx<=(x+3)>>4;cx++)for(int cz=(z-3)>>4;cz<=(z+3)>>4;cz++)level.getChunk(cx,cz);
  var center=new Point(x,y,z,0);var bounds=com.ianblk.zianmanager.core.CenteredZone.bounds(center,3,3,6,0);var zone=new ZoneSpec("smoke_voidfloor","minecraft:the_nether",bounds.first(),bounds.second(),List.of(),List.of("smoke_guard"),1,5,60,"",true,center,3,3,6,0,"Plataforma del vacío");runtime.store().put(zone);
  for(int dx=-3;dx<=3;dx++)for(int dz=-3;dz<=3;dz++)for(int dy=0;dy<=7;dy++)level.setBlockAndUpdate(new BlockPos(x+dx,y+dy,z+dz),net.minecraft.world.level.block.Blocks.AIR.defaultBlockState());
  var prepare=ManagerRuntime.class.getDeclaredMethod("prepare",ZoneSpec.class,UUID.class,int.class,List.class);prepare.setAccessible(true);if(!((List<?>)prepare.invoke(runtime,zone,UUID.randomUUID(),3,List.of())).isEmpty())throw new IllegalStateException("Mobs spawned into empty air without platform");
  for(int dx=-3;dx<=3;dx++)for(int dz=-3;dz<=3;dz++)level.setBlockAndUpdate(new BlockPos(x+dx,y,z+dz),net.minecraft.world.level.block.Blocks.STONE.defaultBlockState());
  var prepared=(List<?>)prepare.invoke(runtime,zone,UUID.randomUUID(),3,List.of());if(prepared.isEmpty())throw new IllegalStateException("Suspended platform cannot prepare mobs");for(var entry:prepared){var method=entry.getClass().getDeclaredMethod("mob");method.setAccessible(true);var mob=(net.minecraft.world.entity.Mob)method.invoke(entry);if(mob.level()!=level || mob.getY()!=y+1)throw new IllegalStateException("Prepared mob ignored dimension or platform floor");}
  var begin=ManagerRuntime.class.getDeclaredMethod("begin",ZoneSpec.class,Set.class,int.class,UUID.class);begin.setAccessible(true);begin.invoke(runtime,zone,Set.of(player.getUUID()),1,UUID.randomUUID());var run=runtime.encounter(zone.id());if(run.spawns().isEmpty())throw new IllegalStateException("Suspended platform in another dimension did not spawn mobs");for(var slot:run.spawns())if(slot.point().y()!=y+1 || player.getServer().overworld().getEntity(slot.uuid())!=null)throw new IllegalStateException("Spawn ignored configured dimension or platform floor");runtime.cancel(zone.id());runtime.store().remove("zone",zone.id());ZianManager.LOGGER.info("Zian Manager suspended platform smoke passed: another dimension, no spawns into void, mobs on platform without natural terrain");}
}
