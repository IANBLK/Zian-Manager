package com.ianblk.zianmanager;
import com.ianblk.zianmanager.core.*;
import com.ianblk.zianmanager.core.Definitions.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;
import java.util.*;
/** Opt-in native fixture: configured destinations, actual NPC action and safe arrival validation. */
public final class DungeonEntrancesSmoke {
 public static void verify(ManagerRuntime runtime,ServerPlayer player)throws Exception{
  var field=ManagerRuntime.class.getDeclaredField("entrances");field.setAccessible(true);var entries=(DungeonEntrances)field.get(runtime);
  var safe=ManagerRuntime.class.getDeclaredMethod("safeArrival",ServerPlayer.class,DungeonEntrances.Arrival.class);safe.setAccessible(true);
  var sessions=ManagerRuntime.class.getDeclaredField("sessions");sessions.setAccessible(true);
  var level=player.serverLevel();var original=player.position();var oldConnection=player.connection;var uuid=UUID.randomUUID();
  String id="smoke_entry";var npc=new NpcSpec(uuid,level.dimension().location().toString(),new Point(3200.5,201,3200.5,0),"Guía de entradas","Prueba","heraldo_real","Entrar",DungeonEntrances.PREFIX+id,0,List.of());
  var entity=ManagerNpcs.NPC.get().create(level);entity.setUUID(uuid);entity.moveTo(3200.5,201,3200.5,0,0);level.getChunkAt(entity.blockPosition());level.addFreshEntity(entity);runtime.store().put(npc);
  var arrivals=new ArrayList<DungeonEntrances.Arrival>();
  for(int i=0;i<3;i++){
   int x=3210+8*i;var floor=new BlockPos(x,200,3200);level.getChunkAt(floor);
   for(int dx=-1;dx<=1;dx++)for(int dz=-1;dz<=1;dz++){var p=floor.offset(dx,0,dz);level.setBlockAndUpdate(p,Blocks.STONE.defaultBlockState());level.setBlockAndUpdate(p.above(),Blocks.AIR.defaultBlockState());level.setBlockAndUpdate(p.above(2),Blocks.AIR.defaultBlockState());}
   arrivals.add(new DungeonEntrances.Arrival("p_"+i,"Entrada "+i,npc.dimension(),new Point(x+.5,201,3200.5,90)));
  }
  entries.put(new DungeonEntrances.Dungeon(id,"Dos entradas y una bloqueada",arrivals));
  level.setBlockAndUpdate(new BlockPos(3226,201,3200),Blocks.STONE.defaultBlockState());
  if((boolean)safe.invoke(runtime,player,arrivals.get(2)))throw new IllegalStateException("Blocked point accepted");
  player.connection=new net.minecraft.server.network.ServerGamePacketListenerImpl(player.getServer(),oldConnection.getConnection(),player,net.minecraft.server.network.CommonListenerCookie.createInitial(player.getGameProfile(),false)){
   @Override public void send(net.minecraft.network.protocol.Packet<?> p){}
   @Override public void send(net.minecraft.network.protocol.Packet<?> p,net.minecraft.network.PacketSendListener listener){}
   @Override public void tick(){}
  };
  try{
   var seen=new HashSet<Integer>();player.getRandom().setSeed(4567);
   for(int i=0;i<20;i++){
    player.moveTo(3200.5,201,3200.5,0,0);runtime.openDialogue(player,npc);
    var session=((Map<?,?>)sessions.get(runtime)).get(player.getUUID());var token=session.getClass().getDeclaredField("token");token.setAccessible(true);
    runtime.action(player,new ManagerNetwork.Action((UUID)token.get(session),"npc_command","{\"button\":\"main\"}"));
    int x=(int)player.getX();if(x!=3210 && x!=3218)throw new IllegalStateException("NPC did not reach configured safe point: "+player.position());seen.add(x);
   }
   if(seen.size()!=2)throw new IllegalStateException("Both random entries were not exercised");
   level.setBlockAndUpdate(new BlockPos(3210,200,3200),Blocks.MAGMA_BLOCK.defaultBlockState());
   if((boolean)safe.invoke(runtime,player,arrivals.get(0)))throw new IllegalStateException("Hazardous point accepted");
   if((boolean)safe.invoke(runtime,player,new DungeonEntrances.Arrival("missing","Missing","missing:world",arrivals.get(0).position())))throw new IllegalStateException("Missing world accepted");
   runtime.request(player,"entrance",""); // non-admin must not obtain a configuration session
   var session=((Map<?,?>)sessions.get(runtime)).get(player.getUUID());
   if(session!=null)throw new IllegalStateException("Non-admin received configuration session");
   ZianManager.LOGGER.info("Zian Manager dungeon entrances smoke passed: two random native NPC destinations, blocked/hazard/missing-world rejection and admin gate");
  }finally{player.connection=oldConnection;player.moveTo(original.x,original.y,original.z,0,0);entity.discard();runtime.store().remove("npc",uuid.toString());entries.remove(id);}
 }
}
