package com.ianblk.zianmanager;
import com.google.gson.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import java.util.*;
/** A personal, single-use claim session; administration uses its separate existing protocol. */
public final class PersonalRewards {
 private record Session(UUID token,long expires,Set<UUID> ids){}
 private static final Map<UUID,Session> sessions=new HashMap<>();
 private static final Map<UUID,Long> requests=new HashMap<>();
 private PersonalRewards(){}
 public static boolean handles(ServerPlayer p,UUID token){var s=sessions.get(p.getUUID());return s!=null&&s.token().equals(token);}
 public static void forget(UUID owner){sessions.remove(owner);requests.remove(owner);}
 public static void clear(){sessions.clear();requests.clear();}
 public static void open(ServerPlayer p){show(p,"");}
 private static boolean allowed(ServerPlayer p){return com.ianblk.zianmanager.permission.ManagerPermissions.allows(p.createCommandSourceStack(),"loot",false);}
 private static void show(ServerPlayer p,String notice){
  try{
   if(!allowed(p))throw new IllegalArgumentException("No tienes acceso a las recompensas");
   long now=System.currentTimeMillis();if(now<requests.getOrDefault(p.getUUID(),0L))return;requests.put(p.getUUID(),now+150);
   var claims=ManagerRuntime.get().loot().pending(p.getUUID());
   var ids=new LinkedHashSet<UUID>();var rows=new JsonArray();
   for(var claim:claims.stream().filter(c->!c.complete()).sorted(java.util.Comparator.comparingLong(c->c.created())).limit(128).toList()){
    var row=new JsonObject();row.addProperty("id",claim.id().toString());row.addProperty("reference",ManagerRuntime.get().loot().reference(p.getUUID(),claim.id()));row.addProperty("origin",claim.trainer());row.addProperty("review",claim.review());rows.add(row);if(!claim.review())ids.add(claim.id());
   }
   var token=UUID.randomUUID();sessions.put(p.getUUID(),new Session(token,now+120000,Set.copyOf(ids)));
   var view=new JsonObject();view.addProperty("type","personal_rewards");view.addProperty("token",token.toString());view.addProperty("notice",notice);view.add("entries",rows);ManagerNetwork.send(p,view.toString());
  }catch(Exception error){p.sendSystemMessage(Messages.format(Component.literal(error.getMessage()==null?"No se pudieron consultar tus premios":error.getMessage())));}
 }
 public static void claim(ServerPlayer p,UUID token,String operation,String json){
  var session=sessions.remove(p.getUUID());if(session==null||!session.token().equals(token)||session.expires()<System.currentTimeMillis())return;
  String notice="";
  try{
   if(!allowed(p)||!operation.equals("claim"))throw new IllegalArgumentException("Acción no permitida");
   UUID id=UUID.fromString(JsonParser.parseString(json).getAsJsonObject().get("id").getAsString());
   if(!session.ids().contains(id))throw new IllegalArgumentException("Este premio no pertenece a esta sesión o requiere revisión");
   ManagerRuntime.get().loot().deliver(p,id);
   notice="Estado actualizado. Las entregas en revisión no se repiten.";
  }catch(Exception error){notice=error.getMessage()==null?"No se pudo completar la entrega":error.getMessage();}
  requests.remove(p.getUUID());show(p,notice);
 }
}
