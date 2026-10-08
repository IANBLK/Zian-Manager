package com.ianblk.zianmanager;
import com.google.gson.*;
import com.ianblk.zianmanager.core.*;
import com.ianblk.zianmanager.core.Definitions.*;
import com.ianblk.zianmanager.core.EncounterLedger.*;
import com.ianblk.zianmanager.permission.ManagerPermissions;
import com.ianblk.zianmanager.reward.*;
import net.minecraft.server.*;
import net.minecraft.server.level.*;
import net.minecraft.world.level.*;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.player.Player;
import net.minecraft.core.*;
import net.minecraft.core.registries.*;
import net.minecraft.resources.*;
import net.minecraft.network.chat.Component;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.world.BossEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.*;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.bus.api.EventPriority;
import java.util.*;

public final class ManagerRuntime {
    private static ManagerRuntime instance;private MinecraftServer server;private ManagerStore store;private EncounterLedger ledger;private LootService loot;private NpcCommands npcCommands;
    private record Death(Mob mob,net.minecraft.world.damagesource.DamageSource source){}
    private final Map<UUID,Death> deaths=new LinkedHashMap<>();
    private final Map<UUID,Mob> previews=new HashMap<>();private final Map<UUID,Session> sessions=new HashMap<>();private final Map<UUID,ServerBossEvent> bars=new HashMap<>();private final Map<UUID,Integer> missing=new HashMap<>();private long ticks;
    private record Session(UUID token,String type,String id,long until,BlockPos pos){}
    public static ManagerRuntime get(){return instance;}
    public ManagerStore store(){ready();return store;}
    public Run encounter(String id){ready();return ledger.get(id);}
    public LootService loot(){ready();return loot;}
    public ManagerRuntime(){
        instance=this;
        NeoForge.EVENT_BUS.addListener((ServerStartedEvent e)->start(e.getServer()));
        NeoForge.EVENT_BUS.addListener((ServerStoppedEvent e)->{bars.values().forEach(ServerBossEvent::removeAllPlayers);bars.clear();previews.values().forEach(Entity::discard);previews.clear();deaths.clear();sessions.clear();missing.clear();server=null;store=null;ledger=null;loot=null;npcCommands=null;});
        NeoForge.EVENT_BUS.addListener(this::tick);
        NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST,this::drops);
        NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST,(net.neoforged.neoforge.event.entity.living.LivingDeathEvent e)->{
            if(ledger!=null && e.getEntity() instanceof Mob mob && mob.getPersistentData().contains("ZianManagerRun"))deaths.put(mob.getUUID(),new Death(mob,e.getSource()));
        });
        NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST,(BlockEvent.BreakEvent e)->{
            if(!ManagerBlocks.isChest(e.getState()))return;
            if(!(e.getPlayer() instanceof ServerPlayer player) || !player.isCreative() || !admin(player) || store==null){e.setCanceled(true);return;}
            try{var chest=store.data().chests().values().stream().filter(c->c.dimension().equals(dimension(player)) && new BlockPos(c.x(),c.y(),c.z()).equals(e.getPos())).findFirst().orElse(null);if(chest!=null)store.remove("chest",chest.uuid().toString());}
            catch(Exception error){e.setCanceled(true);ZianManager.LOGGER.error("Cannot unregister chest; break denied",error);}
        });
        NeoForge.EVENT_BUS.addListener((PlayerInteractEvent.RightClickBlock e)->{
            if(e.getEntity() instanceof ServerPlayer player && player.isShiftKeyDown() && ManagerBlocks.isChest(e.getLevel().getBlockState(e.getPos())) && admin(player)){
                e.setCanceled(true);e.setCancellationResult(net.minecraft.world.InteractionResult.SUCCESS);openChest(player,e.getPos());
            }
        });
        NeoForge.EVENT_BUS.addListener((PlayerInteractEvent.EntityInteract e)->{
            if(e.getEntity() instanceof ServerPlayer player && store!=null){var npc=store.data().npcs().get(e.getTarget().getUUID());if(npc!=null){e.setCanceled(true);e.setCancellationResult(net.minecraft.world.InteractionResult.SUCCESS);openDialogue(player,npc);}}
        });
        NeoForge.EVENT_BUS.addListener((EntityJoinLevelEvent e)->{
            if(e.getLevel().isClientSide)return;var tag=e.getEntity().getPersistentData();if(tag.getBoolean("ZianManagerPreview") && !previews.containsKey(e.getEntity().getUUID())){e.setCanceled(true);return;}if(store!=null && tag.getBoolean("ZianManagerNpc") && !store.data().npcs().containsKey(e.getEntity().getUUID())){e.setCanceled(true);return;}if(ledger==null)return;
            if(tag.contains("ZianManagerRun")){
                var run=ledger.get(tag.getString("ZianManagerZone"));
                if(run==null || !run.uuid().toString().equals(tag.getString("ZianManagerRun")) || run.phase()==Phase.CANCELLED || run.spawns().stream().noneMatch(s->s.uuid().equals(e.getEntity().getUUID()) && !s.defeated())){e.setCanceled(true);}
            }
        });
    }
    private void start(MinecraftServer value){server=value;ticks=0;try{
        var base=server.getWorldPath(LevelResource.ROOT).resolve("data/zianmanager");store=new ManagerStore(base.resolve("definitions.json"));ledger=new EncounterLedger(base.resolve("encounters.json"));for(var c:List.copyOf(store.data().chests().values()))if(c.block().equals("zianmanager:dungeon_chest"))store.put(new ChestSpec(c.uuid(),c.dimension(),c.x(),c.y(),c.z(),c.loot(),c.minutes(),"zianmanager:loot_common_crate",c.facing()));loot=new LootService(server,store);npcCommands=new NpcCommands(base.resolve("npc_commands.json"));
        ZianManager.LOGGER.info("Zian Manager ready: {} mobs, {} zones, {} loot tables; chest protected, personal loot",store.data().mobs().size(),store.data().zones().size(),store.data().loot().size());
    }catch(Exception error){store=null;ledger=null;loot=null;npcCommands=null;ZianManager.LOGGER.error("Zian Manager unavailable; source files preserved",error);}}
    private void ready(){if(server==null || store==null || ledger==null || loot==null)throw new IllegalStateException("Zian Manager no está listo; revisa sus archivos");}
    private boolean admin(ServerPlayer player){return ManagerPermissions.allows(player.createCommandSourceStack(),"admin",true);}
    public ServerLevel level(String dimension){return server.getLevel(ResourceKey.create(Registries.DIMENSION,ResourceLocation.parse(dimension)));}
    private static Point here(ServerPlayer player){return new Point(player.getX(),player.getY(),player.getZ(),player.getYRot());}
    private static String dimension(ServerPlayer player){return player.serverLevel().dimension().location().toString();}
    private static JsonObject fields(String json){var parsed=JsonParser.parseString(json);if(!parsed.isJsonObject() || parsed.getAsJsonObject().size()>24)throw new IllegalArgumentException("Formulario inválido");return parsed.getAsJsonObject();}
    private static String str(JsonObject f,String key,String fallback){return f.has(key)?f.get(key).getAsString():fallback;}
    private static int num(JsonObject f,String key,int fallback){return Integer.parseInt(str(f,key,""+fallback));}
    private static double dec(JsonObject f,String key,double fallback){return Double.parseDouble(str(f,key,""+fallback));}
    public void request(ServerPlayer player,String type,String id){
        try{ready();if(!admin(player))throw new IllegalArgumentException("Necesitas zianmanager.admin");view(player,type,id,null,"");}
        catch(Exception error){player.sendSystemMessage(Component.literal(error.getMessage()));}
    }
    public void openChest(ServerPlayer player,BlockPos pos){
        try{ready();var chest=store.data().chests().values().stream().filter(c->c.dimension().equals(dimension(player)) && new BlockPos(c.x(),c.y(),c.z()).equals(pos)).findFirst().orElse(null);
            if(admin(player) && (player.isShiftKeyDown() || chest==null)){view(player,"chest",chest==null?"":chest.uuid().toString(),pos,"");return;}
            if(!ManagerPermissions.allows(player.createCommandSourceStack(),"loot",false))throw new IllegalArgumentException("No tienes permiso para abrir este cofre");
            if(chest==null)throw new IllegalArgumentException("Cofre sin configurar. El administrador debe usar Shift + clic derecho.");
            long wait=loot.remaining(player,"chest."+chest.uuid(),chest.minutes());if(wait>0)throw new IllegalArgumentException("Loot disponible en "+((wait+59999)/60000)+" minuto(s)");
            var claim=loot.reserve(player,"chest."+chest.uuid(),chest.loot(),chest.minutes(),pos);
            if(claim.review()){player.sendSystemMessage(Component.literal("Entrega pendiente de revisión; no se repetirá."));return;}player.serverLevel().blockEvent(pos,player.serverLevel().getBlockState(pos).getBlock(),1,1);loot.deliver(player,claim.id());
        }catch(Exception error){player.sendSystemMessage(Component.literal(error.getMessage()));}
    }
    public void view(ServerPlayer player,String type,String id,BlockPos context,String notice){
        UUID token=UUID.randomUUID();sessions.put(player.getUUID(),new Session(token,type,id,System.currentTimeMillis()+300000,context));
        var out=new JsonObject();out.addProperty("type",type);out.addProperty("id",id);out.addProperty("token",token.toString());out.addProperty("title","Zian Manager · "+type);out.addProperty("notice",notice);
        var f=new JsonObject();var list=new JsonArray();
        if(type.equals("root")){
            for(String section:List.of("mobs","loot","zones","chests","npcs")){var e=new JsonObject();e.addProperty("id",section);e.addProperty("name",section);list.add(e);}
        }else if(type.endsWith("s") || type.equals("lootlist")){
            var ids=switch(type){case "mobs"->store.data().mobs().keySet();case "zones"->store.data().zones().keySet();case "lootlist"->store.data().loot().keySet();default->Set.<String>of();};
            ids.stream().sorted().forEach(k->{var e=new JsonObject();e.addProperty("id",k);e.addProperty("name",k);list.add(e);});
            if(type.equals("chests"))store.data().chests().values().stream().filter(c->c.dimension().equals(dimension(player))).forEach(c->{var e=new JsonObject();e.addProperty("id",c.uuid().toString());e.addProperty("name",c.loot()+" · "+c.x()+","+c.y()+","+c.z());list.add(e);});
            if(type.equals("npcs"))store.data().npcs().values().stream().filter(c->c.dimension().equals(dimension(player))).forEach(c->{var e=new JsonObject();e.addProperty("id",c.uuid().toString());e.addProperty("name",c.name());list.add(e);});
        }else if(type.equals("mob")){
            var m=store.data().mobs().get(id);field(f,"id",id.isEmpty()?"mob_"+UUID.randomUUID().toString().substring(0,8):id);field(f,"name",m==null?"Guardián":m.name());field(f,"entity",m==null?"minecraft:zombie":m.entity());field(f,"health",m==null?40:m.health());field(f,"damage",m==null?6:m.damage());field(f,"armor",m==null?4:m.armor());field(f,"toughness",m==null?0:m.toughness());field(f,"knockback",m==null?0:m.knockback());field(f,"boss",m!=null && m.boss());field(f,"loot",m==null?"":m.loot());field(f,"slot","mainhand");field(f,"effects",m==null?"":m.effects().stream().map(e->e.id()+","+e.amplifier()+","+e.seconds()).collect(java.util.stream.Collectors.joining(";")));
            if(m!=null)m.equipment().forEach((slot,item)->{var e=new JsonObject();e.addProperty("id",slot);e.addProperty("name",slot+": "+LootService.item(player,item).getHoverName().getString());list.add(e);});
        }else if(type.equals("loot")){
            var l=store.data().loot().get(id);field(f,"id",id.isEmpty()?"loot_"+UUID.randomUUID().toString().substring(0,8):id);field(f,"category",l==null?"MOB":l.category());field(f,"rolls",l==null?2:l.rolls());field(f,"table",l==null?"minecraft:chests/simple_dungeon":l.table());field(f,"weight",10);field(f,"min",1);field(f,"max",1);field(f,"entry",0);
            if(l!=null){int total=l.entries().stream().mapToInt(LootEntry::weight).sum();for(int i=0;i<l.entries().size();i++){var entry=l.entries().get(i);var e=new JsonObject();e.addProperty("id",""+i);e.addProperty("icon",BuiltInRegistries.ITEM.getKey(LootService.item(player,entry.item()).getItem()).toString());e.addProperty("display",LootService.item(player,entry.item()).getHoverName().getString());e.addProperty("weight",entry.weight());e.addProperty("min",entry.min());e.addProperty("max",entry.max());e.addProperty("name",i+": "+LootService.item(player,entry.item()).getHoverName().getString()+" · "+String.format(java.util.Locale.ROOT,"%.1f%%",100.0*entry.weight()/total)+" · "+entry.min()+"–"+entry.max());list.add(e);}}
        }else if(type.equals("zone")){
            var z=store.data().zones().get(id);field(f,"id",id.isEmpty()?"zona_"+UUID.randomUUID().toString().substring(0,8):id);field(f,"team",z==null?"":String.join(",",z.team()));field(f,"waves",z==null?1:z.waves());field(f,"pauseSeconds",z==null?5:z.pauseSeconds());field(f,"respawnSeconds",z==null?600:z.respawnSeconds());field(f,"completionLoot",z==null?"":z.completionLoot());field(f,"enabled",z!=null && z.enabled());field(f,"radiusX",z!=null && z.center()!=null?z.radiusX():z!=null && z.first()!=null && z.second()!=null?Math.max(1,(int)Math.ceil(Math.abs(z.first().x()-z.second().x())/2)):3);field(f,"radiusZ",z!=null && z.center()!=null?z.radiusZ():z!=null && z.first()!=null && z.second()!=null?Math.max(1,(int)Math.ceil(Math.abs(z.first().z()-z.second().z())/2)):3);field(f,"above",z!=null && z.center()!=null?z.above():z!=null && z.first()!=null && z.second()!=null?Math.max(1,(int)Math.ceil(Math.abs(z.first().y()-z.second().y()))+1):6);field(f,"below",z!=null && z.center()!=null?z.below():0);
            if(z!=null){out.add("zonePreview",com.ianblk.zianmanager.core.AtomicJson.GSON.toJsonTree(z));out.addProperty("notice",notice+" · Puntos: "+z.points().size()+" · Centro: "+(z.center()==null?"pendiente":((int)z.center().x()+", "+(int)z.center().y()+", "+(int)z.center().z()))+" · "+(ledger.get(id)==null?"Sin encuentro":ledger.get(id).phase()));}
        }else if(type.equals("chest")){
            var c=id.isEmpty()?null:store.data().chests().get(UUID.fromString(id));field(f,"loot",c==null?"":c.loot());field(f,"minutes",c==null?10:c.minutes());if(context==null && c!=null){context=new BlockPos(c.x(),c.y(),c.z());sessions.put(player.getUUID(),new Session(token,type,id,System.currentTimeMillis()+300000,context));}
            out.addProperty("notice","Shift + clic derecho: editar · retirada: creativo con permiso admin");
        }else if(type.equals("npc")){
            var n=id.isEmpty()?null:store.data().npcs().get(UUID.fromString(id));field(f,"name",n==null?"Guía":n.name());field(f,"text",n==null?"Bienvenido a la dungeon.":n.text());field(f,"skin",n==null?"heraldo_real":n.skin());field(f,"button",n==null?"Entrar":n.button());field(f,"command",n==null?"":n.command());field(f,"cooldownSeconds",n==null?60:n.cooldownSeconds());field(f,"extraActions",AtomicJson.GSON.toJson(n==null?List.of():n.extraActions()));
        }else throw new IllegalArgumentException("Pantalla desconocida");
        var choices=new JsonObject();var tables=new JsonArray();store.data().loot().values().stream().sorted(java.util.Comparator.comparing(LootSpec::id)).forEach(l->{var e=new JsonObject();e.addProperty("id",l.id());e.addProperty("name",l.id()+" · "+l.category()+" · "+l.rolls()+" objetos");tables.add(e);});choices.add("loot",tables);
        var mobs=new JsonArray();store.data().mobs().values().stream().sorted(java.util.Comparator.comparing(MobSpec::name)).forEach(m->{var e=new JsonObject();e.addProperty("id",m.id());e.addProperty("name",m.name()+(m.boss()?" · Jefe":""));mobs.add(e);});choices.add("mobs",mobs);out.add("choices",choices);
        out.add("fields",f);out.add("entries",list);ManagerNetwork.send(player,out.toString());
    }
    private static void field(JsonObject object,String key,Object value){object.addProperty(key,String.valueOf(value));}
    public void action(ServerPlayer player,ManagerNetwork.Action action){
        Session session=sessions.get(player.getUUID());if(session==null || !session.token.equals(action.token()) || session.until<System.currentTimeMillis())return;
        sessions.remove(player.getUUID());String id=session.id;final String selected=id;String notice="Guardado";
        try{ready();var f=fields(action.json());
            if(session.type.equals("claim")){
                if(!action.operation().equals("claim") || session.pos==null || player.distanceToSqr(Vec3(session.pos))>36 || !ManagerBlocks.isChest(player.serverLevel().getBlockState(session.pos)) || !ManagerPermissions.allows(player.createCommandSourceStack(),"loot",false))throw new IllegalArgumentException("Cofre no disponible");
                loot.deliver(player,UUID.fromString(id));return;
            }
            if(session.type.equals("dialogue")){
                if(!action.operation().equals("npc_command"))throw new IllegalArgumentException("Acción inválida");
                var npc=store.data().npcs().get(UUID.fromString(id));var entity=player.serverLevel().getEntity(UUID.fromString(id));
                if(npc==null || entity==null || entity.distanceToSqr(player)>36 || !npc.dimension().equals(dimension(player)) || !ManagerPermissions.allows(player.createCommandSourceStack(),"npc",false))throw new IllegalArgumentException("NPC no disponible");
                String buttonId=str(f,"button","main");String chosenCommand;int cooldown;
                if(buttonId.equals("main")){chosenCommand=npc.command();cooldown=npc.cooldownSeconds();}else{var selectedButton=npc.extraActions().stream().filter(b->b.id().equals(buttonId)).findFirst().orElseThrow(()->new IllegalArgumentException("Botón inexistente"));chosenCommand=selectedButton.command();cooldown=selectedButton.cooldownSeconds();}
                if(chosenCommand.isBlank())throw new IllegalArgumentException("Botón sin comando");
                npcCommands.begin(npc.uuid(),player.getUUID(),buttonId,cooldown,System.currentTimeMillis());
                String command=chosenCommand.replace("{player}",player.getGameProfile().getName());
                String root=command.split(" ",2)[0];if(root.startsWith("minecraft:") && server.getCommands().getDispatcher().getRoot().getChild(root)==null && server.getCommands().getDispatcher().getRoot().getChild(root.substring(10))!=null)command=command.substring(10);
                try{int result=server.getCommands().getDispatcher().execute(command,server.createCommandSourceStack().withLevel(player.serverLevel()).withPosition(player.position()).withPermission(4));npcCommands.complete(npc.uuid(),player.getUUID(),buttonId,result>0,System.currentTimeMillis());ZianManager.LOGGER.info("[ZIAN-MANAGER] action=npc_command npc={} player={} result={}",npc.uuid(),player.getUUID(),result>0?"DONE":"FAILED");if(result<=0)throw new IllegalArgumentException("El comando no confirmó éxito; requiere revisión.");}
                catch(Exception error){ZianManager.LOGGER.error("NPC command execution needs review for NPC {} player {}",npc.uuid(),player.getUUID(),error);throw error;}
                return;
            }
            if(!admin(player))throw new IllegalArgumentException("No tienes permiso");
            String operation=action.operation();
            if(operation.equals("delete"))throw new IllegalArgumentException("Confirma con Eliminar confirmado");
            if(operation.equals("delete_confirmed")){
                if(session.type.equals("zone"))cancel(id);
                if(session.type.equals("mob") && store.data().zones().values().stream().anyMatch(z->z.team().contains(selected)))throw new IllegalArgumentException("La plantilla está usada por una zona");
                if(session.type.equals("loot") && (store.data().mobs().values().stream().anyMatch(m->m.loot().equals(selected)) || store.data().chests().values().stream().anyMatch(c->c.loot().equals(selected)) || store.data().zones().values().stream().anyMatch(z->z.completionLoot().equals(selected))))throw new IllegalArgumentException("Loot usado por mobs o cofres");
                if(session.type.equals("npc")){var n=store.data().npcs().get(UUID.fromString(id));if(n!=null){var entity=level(n.dimension()).getEntity(n.uuid());if(entity!=null)entity.discard();}}
                if(session.type.equals("chest")){var c=id.isEmpty()?null:store.data().chests().get(UUID.fromString(id));BlockPos pos=session.pos;if(pos==null)throw new IllegalArgumentException("Selecciona el cofre");if(c!=null)store.remove("chest",id);player.serverLevel().setBlockAndUpdate(pos,net.minecraft.world.level.block.Blocks.AIR.defaultBlockState());request(player,"chests","");return;}
                store.remove(session.type,id);request(player,session.type.equals("loot")?"lootlist":session.type+"s","");return;
            }
            if(session.type.equals("mob")){
                if(activeUses(id))throw new IllegalArgumentException("Finaliza o cancela el encuentro que usa este mob");
                String chosen=str(f,"id",id);if(!id.isEmpty() && !id.equals(chosen) || id.isEmpty() && store.data().mobs().containsKey(chosen))throw new IllegalArgumentException("ID existente o cambiado");
                var old=store.data().mobs().get(chosen);var effects=new ArrayList<Effect>();String text=str(f,"effects","");if(!text.isBlank())for(String part:text.split(";")){var bits=part.split(",");if(bits.length!=3)throw new IllegalArgumentException("Efecto: minecraft:speed,0,600");effects.add(new Effect(bits[0].trim(),Integer.parseInt(bits[1].trim()),Integer.parseInt(bits[2].trim())));}
                var mob=new MobSpec(chosen,str(f,"name","Guardián"),str(f,"entity","minecraft:zombie"),dec(f,"health",40),dec(f,"damage",6),dec(f,"armor",4),dec(f,"toughness",0),dec(f,"knockback",0),effects,old==null?Map.of():old.equipment(),str(f,"loot",""),Boolean.parseBoolean(str(f,"boss","false")));
                if(operation.equals("equip")){var held=player.getMainHandItem().copy();if(held.isEmpty())throw new IllegalArgumentException("Sostén un objeto");held.setCount(1);mob=mob.equip(str(f,"slot","mainhand"),held.save(player.registryAccess()).toString());}
                validateMob(mob,player.serverLevel());store.put(mob);id=chosen;
                if(operation.equals("preview")){var preview=spawnMob(player.serverLevel(),mob,here(player),UUID.randomUUID());preview.setCustomName(Component.literal("Vista previa · "+mob.name()));preview.setNoAi(true);preview.setInvulnerable(true);preview.getPersistentData().putBoolean("ZianManagerPreview",true);previews.put(preview.getUUID(),preview);player.serverLevel().addFreshEntity(preview);notice="Vista previa inmóvil por 20 segundos; sin loot";}
            }else if(session.type.equals("loot")){
                String chosen=str(f,"id",id);if(!id.isEmpty() && !id.equals(chosen) || id.isEmpty() && store.data().loot().containsKey(chosen))throw new IllegalArgumentException("ID existente o cambiado");
                var old=store.data().loot().get(chosen);var entries=new ArrayList<>(old==null?List.<LootEntry>of():old.entries());String table=str(f,"table",old==null?"minecraft:chests/simple_dungeon":old.table());
                if(operation.equals("add")){var held=player.getMainHandItem().copy();if(ManagerEquipment.retired(held.getItem()))throw new IllegalArgumentException("Este objeto fue retirado del catálogo.");if(held.isEmpty())throw new IllegalArgumentException("Sostén un objeto");int max=num(f,"max",held.getCount());if(max>held.getMaxStackSize())throw new IllegalArgumentException("Cantidad supera el stack máximo");held.setCount(1);entries.add(new LootEntry(held.save(player.registryAccess()).toString(),num(f,"weight",10),num(f,"min",1),max));table="";}
                if(operation.equals("remove_entry")){int index=num(f,"entry",0);if(index<0 || index>=entries.size())throw new IllegalArgumentException("Índice incorrecto");entries.remove(index);}
                if(operation.equals("edit_entry")){int index=num(f,"entry",0);if(index<0 || index>=entries.size())throw new IllegalArgumentException("Índice incorrecto");var before=entries.get(index);int max=num(f,"max",1);if(max>LootService.item(player,before.item()).getMaxStackSize())throw new IllegalArgumentException("Cantidad fuera de límite");entries.set(index,new LootEntry(before.item(),num(f,"weight",10),num(f,"min",1),max));}
                store.put(new LootSpec(chosen,str(f,"category","MOB"),num(f,"rolls",2),entries,table));id=chosen;
            }else if(session.type.equals("zone")){
                String chosen=str(f,"id",id);if(!id.isEmpty() && !id.equals(chosen) || id.isEmpty() && store.data().zones().containsKey(chosen))throw new IllegalArgumentException("ID existente o cambiado");
                var current=ledger.get(chosen);if(current!=null && (current.phase()==Phase.ACTIVE || current.phase()==Phase.WAITING) && !operation.equals("cancel"))throw new IllegalArgumentException("Cancela el encuentro antes de editar");
                var old=store.data().zones().get(chosen);Point first=old==null?null:old.first(),second=old==null?null:old.second();var points=new ArrayList<>(old==null?List.<Point>of():old.points());
                Point center=old==null?null:old.center();int radiusX=num(f,"radiusX",3),radiusZ=num(f,"radiusZ",3),above=num(f,"above",6),below=num(f,"below",0);
                if(operation.equals("center") && center==null){var pos=player.blockPosition().below();center=new Point(pos.getX(),pos.getY(),pos.getZ(),0);}
                if(operation.equals("resize") && center==null){if(first==null || second==null)throw new IllegalArgumentException("Pulsa Crear zona de dungeon desde su centro");center=new Point(Math.floor((first.x()+second.x())/2),Math.floor(Math.min(first.y(),second.y())),Math.floor((first.z()+second.z())/2),0);}
                if(operation.equals("center") || operation.equals("resize")){var bounds=CenteredZone.bounds(center,radiusX,radiusZ,above,below);first=bounds.first();second=bounds.second();}
                else if(center!=null){radiusX=old.radiusX();radiusZ=old.radiusZ();above=old.above();below=old.below();}
                if(operation.equals("point"))points.add(here(player));if(operation.equals("clear_points"))points.clear();
                var team=Arrays.stream(str(f,"team","").split(",")).map(String::trim).filter(s->!s.isEmpty()).toList();for(String mob:team)if(!store.data().mobs().containsKey(mob))throw new IllegalArgumentException("Mob inexistente: "+mob);
                if(!str(f,"completionLoot","").isEmpty() && !store.data().loot().containsKey(str(f,"completionLoot","")))throw new IllegalArgumentException("Loot de finalización inexistente");
                var zone=new ZoneSpec(chosen,old==null?dimension(player):old.dimension(),first,second,points,team,num(f,"waves",1),num(f,"pauseSeconds",5),num(f,"respawnSeconds",600),str(f,"completionLoot",""),Boolean.parseBoolean(str(f,"enabled","false")),center,center==null?0:radiusX,center==null?0:radiusZ,center==null?0:above,center==null?0:below);
                store.put(zone);id=chosen;if(operation.equals("cancel"))cancel(chosen);if(operation.equals("test")){if(!zone.enabled())throw new IllegalArgumentException("Habilita la zona y sus puntos");begin(zone,Set.of(player.getUUID()),1,UUID.randomUUID());}
            }else if(session.type.equals("chest")){
                if(session.pos==null || player.distanceToSqr(Vec3(session.pos))>64 || !ManagerBlocks.isChest(player.serverLevel().getBlockState(session.pos)))throw new IllegalArgumentException("Acércate al cofre y usa Shift + clic derecho");
                String preset=str(f,"loot","");if(!store.data().loot().containsKey(preset))throw new IllegalArgumentException("Loot inexistente");UUID uuid=id.isEmpty()?UUID.randomUUID():UUID.fromString(id);store.put(new ChestSpec(uuid,dimension(player),session.pos.getX(),session.pos.getY(),session.pos.getZ(),preset,num(f,"minutes",10),BuiltInRegistries.BLOCK.getKey(player.serverLevel().getBlockState(session.pos).getBlock()).toString(),player.serverLevel().getBlockState(session.pos).getValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.HORIZONTAL_FACING).getName()));id=uuid.toString();
            }else if(session.type.equals("npc")){
                UUID uuid=id.isEmpty()?UUID.randomUUID():UUID.fromString(id);var old=store.data().npcs().get(uuid);Point point=old==null || operation.equals("move")?here(player):old.point();var npc=new NpcSpec(uuid,old==null?dimension(player):old.dimension(),point,str(f,"name","Guía"),str(f,"text","Bienvenido"),str(f,"skin","heraldo_real"),str(f,"button","Continuar"),str(f,"command",""),num(f,"cooldownSeconds",60),Arrays.asList(AtomicJson.GSON.fromJson(str(f,"extraActions","[]"),NpcButton[].class)));store.put(npc);id=uuid.toString();placeNpc(npc);if(operation.equals("reset_npc_actions")){npcCommands.reset(uuid);notice="Usos del NPC restablecidos por el administrador";}
            }
            ZianManager.LOGGER.info("[ZIAN-MANAGER] admin={} type={} id={} action={}",player.getUUID(),session.type,id,action.operation());view(player,session.type,id,session.pos,notice);
        }catch(Exception error){player.sendSystemMessage(Component.literal(error.getMessage()==null?"No se pudo aplicar":error.getMessage()));if(admin(player))try{view(player,session.type,id,session.pos,error.getMessage());}catch(Exception ignored){}}
    }
    private static net.minecraft.world.phys.Vec3 Vec3(BlockPos pos){return net.minecraft.world.phys.Vec3.atCenterOf(pos);}
    private boolean activeUses(String template){return !template.isEmpty() && ledger.all().stream().anyMatch(r->(r.phase()==Phase.ACTIVE || r.phase()==Phase.WAITING) && store.data().zones().get(r.zone())!=null && store.data().zones().get(r.zone()).team().contains(template));}
    public void validateMob(MobSpec spec,ServerLevel level){if(!BuiltInRegistries.ENTITY_TYPE.containsKey(ResourceLocation.parse(spec.entity())))throw new IllegalArgumentException("Entidad no instalada: "+spec.entity());if(!spec.loot().isEmpty() && !store.data().loot().containsKey(spec.loot()))throw new IllegalArgumentException("Loot inexistente: "+spec.loot());var entity=BuiltInRegistries.ENTITY_TYPE.get(ResourceLocation.parse(spec.entity())).create(level);if(!(entity instanceof Mob))throw new IllegalArgumentException("La entidad no es un mob compatible");for(var effect:spec.effects())if(BuiltInRegistries.MOB_EFFECT.getHolder(ResourceLocation.parse(effect.id())).isEmpty())throw new IllegalArgumentException("Efecto inexistente: "+effect.id());}
    public Mob spawnMob(ServerLevel level,MobSpec spec,Point pos,UUID uuid){
        if(!BuiltInRegistries.ENTITY_TYPE.containsKey(ResourceLocation.parse(spec.entity())))throw new IllegalArgumentException("Entidad no instalada: "+spec.entity());
        var entity=BuiltInRegistries.ENTITY_TYPE.get(ResourceLocation.parse(spec.entity())).create(level);if(!(entity instanceof Mob mob))throw new IllegalArgumentException("Entidad no compatible");
        mob.setUUID(uuid);mob.moveTo(pos.x(),pos.y(),pos.z(),pos.yaw(),0);net.neoforged.neoforge.event.EventHooks.finalizeMobSpawn(mob,level,level.getCurrentDifficultyAt(mob.blockPosition()),MobSpawnType.TRIGGERED,null);if(mob.isSpawnCancelled())throw new IllegalArgumentException("Otro mod canceló la aparición");mob.setCustomName(Component.literal(spec.name()));mob.setCustomNameVisible(true);mob.setPersistenceRequired();
        attribute(mob,Attributes.MAX_HEALTH,spec.health());attribute(mob,Attributes.ATTACK_DAMAGE,spec.damage());attribute(mob,Attributes.ARMOR,spec.armor());attribute(mob,Attributes.ARMOR_TOUGHNESS,spec.toughness());attribute(mob,Attributes.KNOCKBACK_RESISTANCE,spec.knockback());mob.setHealth(mob.getMaxHealth());
        spec.equipment().forEach((slot,item)->{var stack=ItemStack.parseOptional(server.registryAccess(),parseTag(item));mob.setItemSlot(EquipmentSlot.byName(slot),stack);mob.setDropChance(EquipmentSlot.byName(slot),0);});
        for(var effect:spec.effects())mob.addEffect(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.getHolder(ResourceLocation.parse(effect.id())).orElseThrow(),effect.seconds()*20,effect.amplifier()));
        return mob;
    }
    private static CompoundTag parseTag(String item){try{return net.minecraft.nbt.TagParser.parseTag(item);}catch(Exception error){throw new IllegalArgumentException("Equipo ilegible",error);}}
    private static void attribute(Mob mob,Holder<Attribute> attribute,double value){var instance=mob.getAttribute(attribute);if(instance!=null)instance.setBaseValue(value);}
    public void cancel(String id) throws Exception{var zone=store.data().zones().get(id);if(zone!=null && zone.enabled())store.put(new ZoneSpec(zone.id(),zone.dimension(),zone.first(),zone.second(),zone.points(),zone.team(),zone.waves(),zone.pauseSeconds(),zone.respawnSeconds(),zone.completionLoot(),false,zone.center(),zone.radiusX(),zone.radiusZ(),zone.above(),zone.below()));var run=ledger.get(id);if(run==null)return;ledger.put(new Run(run.uuid(),id,run.wave(),Phase.CANCELLED,0,run.spawns(),run.players()));for(var level:server.getAllLevels())for(var slot:run.spawns()){var entity=level.getEntity(slot.uuid());if(entity!=null)entity.discard();}}
    private void begin(ZoneSpec zone,Set<UUID> players,int wave,UUID runId) throws Exception{
        var level=level(zone.dimension());if(level==null)throw new IllegalArgumentException("Dimensión no disponible");var slots=new ArrayList<Spawn>();var mobs=new ArrayList<Mob>();
        for(int i=0;i<zone.team().size();i++){var pos=zone.points().get(i);BlockPos at=BlockPos.containing(pos.x(),pos.y(),pos.z());if(!level.getChunkSource().hasChunk(at.getX()>>4,at.getZ()>>4))throw new IllegalArgumentException("Punto de aparición no cargado");var uuid=UUID.randomUUID();var mob=spawnMob(level,store.data().mobs().get(zone.team().get(i)),pos,uuid);if(!level.noCollision(mob) || !level.getWorldBorder().isWithinBounds(mob.getBoundingBox()))throw new IllegalArgumentException("Punto de aparición bloqueado");
            slots.add(new Spawn(uuid,zone.team().get(i),pos,false));mob.getPersistentData().putString("ZianManagerRun",runId.toString());mob.getPersistentData().putString("ZianManagerZone",zone.id());mobs.add(mob);
        }
        ledger.put(new Run(runId,zone.id(),wave,Phase.ACTIVE,0,slots,players));
        for(var mob:mobs)if(!level.addFreshEntity(mob))throw new IllegalStateException("No se pudo añadir un mob; requiere revisión");
        ZianManager.LOGGER.info("[ZIAN-MANAGER] zone={} run={} wave={} mobs={}",zone.id(),runId,wave,mobs.size());
    }
    private void drops(LivingDropsEvent event){
        if(event.getEntity().getPersistentData().getBoolean("ZianManagerPreview")){event.getDrops().clear();event.setCanceled(true);return;}
        if(ledger==null)return;var tag=event.getEntity().getPersistentData();if(!tag.contains("ZianManagerRun"))return;
        var run=ledger.get(tag.getString("ZianManagerZone"));if(run==null || !run.uuid().toString().equals(tag.getString("ZianManagerRun")))return;
        var slot=run.spawns().stream().filter(s->s.uuid().equals(event.getEntity().getUUID())).findFirst().orElse(null);
        if(slot!=null && !store.data().mobs().get(slot.template()).loot().isEmpty()){event.getDrops().clear();event.setCanceled(true);}
    }
    private void died(Death death){
        var mob=death.mob();if(mob.isAlive())return;var tag=mob.getPersistentData();
        try{String zoneId=tag.getString("ZianManagerZone");var zone=store.data().zones().get(zoneId);var run=ledger.get(zoneId);if(zone==null || run==null || !run.uuid().toString().equals(tag.getString("ZianManagerRun")))return;
            var slot=run.spawns().stream().filter(s->s.uuid().equals(mob.getUUID())).findFirst().orElse(null);if(slot==null || slot.defeated())return;
            var spec=store.data().mobs().get(slot.template());
            if(!ledger.defeated(zoneId,run.uuid(),mob.getUUID(),zone.waves(),zone.pauseSeconds(),zone.respawnSeconds(),System.currentTimeMillis()))return;
            if(death.source().getEntity() instanceof ServerPlayer player && !spec.loot().isEmpty())loot.grant(player,"mob."+mob.getUUID(),spec.loot(),0,mob.blockPosition());
            if(ledger.get(zoneId).phase()==Phase.COMPLETE && !zone.completionLoot().isEmpty())for(UUID uuid:run.players()){
                var player=server.getPlayerList().getPlayer(uuid);if(player!=null && zone.contains(dimension(player),player.getX(),player.getY(),player.getZ()))loot.grant(player,"zone."+run.uuid(),zone.completionLoot(),0,mob.blockPosition());
            }
        }catch(Exception error){ZianManager.LOGGER.error("Encounter death/reward needs review",error);}
    }
    public void openDialogue(ServerPlayer player,NpcSpec npc){
        if(!ManagerPermissions.allows(player.createCommandSourceStack(),"npc",false)){player.sendSystemMessage(Component.literal("No tienes permiso para hablar con este NPC."));return;}
        UUID token=UUID.randomUUID();sessions.put(player.getUUID(),new Session(token,"dialogue",npc.uuid().toString(),System.currentTimeMillis()+300000,null));
        var out=new JsonObject();out.addProperty("type","dialogue");out.addProperty("token",token.toString());out.addProperty("title",npc.name());out.addProperty("text",npc.text().replace("\\n","\n"));out.addProperty("button",npc.button());var actions=new JsonArray();if(!npc.command().isBlank()){var a=new JsonObject();a.addProperty("id","main");a.addProperty("label",npc.button());actions.add(a);}for(var button:npc.extraActions()){var a=new JsonObject();a.addProperty("id",button.id());a.addProperty("label",button.label());actions.add(a);}out.add("actions",actions);out.addProperty("available",!actions.isEmpty());ManagerNetwork.send(player,out.toString());
    }
    private void placeNpc(NpcSpec npc){var level=level(npc.dimension());if(level==null || !level.getChunkSource().hasChunk(((int)npc.point().x())>>4,((int)npc.point().z())>>4))return;var entity=level.getEntity(npc.uuid());
        if(entity!=null && !(entity instanceof ManagerNpcs.DialogueNpc)){entity.discard();entity=null;}
        if(entity==null){var mob=ManagerNpcs.NPC.get().create(level);if(mob==null)return;mob.setUUID(npc.uuid());mob.getPersistentData().putBoolean("ZianManagerNpc",true);entity=mob;entity.moveTo(npc.point().x(),npc.point().y(),npc.point().z(),npc.point().yaw(),0);level.addFreshEntity(entity);}
        ((ManagerNpcs.DialogueNpc)entity).skin(npc.skin());entity.setCustomName(Component.literal(npc.name()));entity.setCustomNameVisible(true);entity.teleportTo(npc.point().x(),npc.point().y(),npc.point().z());entity.setYRot(npc.point().yaw());if(entity instanceof LivingEntity living){living.setYHeadRot(npc.point().yaw());living.setYBodyRot(npc.point().yaw());}
    }
    private void tick(ServerTickEvent.Post event){
        if(event.getServer()!=server || store==null)return;for(var death:List.copyOf(deaths.values()))died(death);deaths.clear();if(++ticks%20!=0)return;long now=System.currentTimeMillis();sessions.values().removeIf(s->s.until<now);
        try{
            previews.entrySet().removeIf(e->{if(e.getValue().tickCount>400 || e.getValue().isRemoved()){e.getValue().discard();return true;}return false;});
            for(var chest:store.data().chests().values()){var level=level(chest.dimension());var pos=new BlockPos(chest.x(),chest.y(),chest.z());if(level!=null && level.getChunkSource().hasChunk(pos.getX()>>4,pos.getZ()>>4) && !level.getBlockState(pos).is(ManagerBlocks.chestState(chest.block()).getBlock()))level.setBlockAndUpdate(pos,ManagerBlocks.chestState(chest.block()).setValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.HORIZONTAL_FACING,net.minecraft.core.Direction.byName(chest.facing())));}
            if(ticks%100==0)store.data().npcs().values().forEach(this::placeNpc);
            for(var zone:store.data().zones().values()){
                if(!zone.enabled())continue;Set<UUID> players=new LinkedHashSet<>();for(var player:server.getPlayerList().getPlayers())if(!player.isSpectator() && !player.isCreative() && zone.contains(dimension(player),player.getX(),player.getY(),player.getZ()))players.add(player.getUUID());if(players.isEmpty())continue;
                var run=ledger.get(zone.id());if(run==null || (run.phase()==Phase.COMPLETE || run.phase()==Phase.CANCELLED) && now>=run.readyAt()){begin(zone,players,1,UUID.randomUUID());continue;}
                if(run.phase()==Phase.WAITING && now>=run.readyAt()){begin(zone,players,run.wave()+1,run.uuid());continue;}
                if(run.phase()!=Phase.ACTIVE)continue;var participants=new LinkedHashSet<>(run.players());participants.addAll(players);if(!participants.equals(run.players())){ledger.put(new Run(run.uuid(),run.zone(),run.wave(),run.phase(),run.readyAt(),run.spawns(),participants));run=ledger.get(zone.id());}
                var level=level(zone.dimension());for(var slot:run.spawns())if(!slot.defeated()){
                    var entity=level.getEntity(slot.uuid());var point=slot.point();BlockPos pos=BlockPos.containing(point.x(),point.y(),point.z());
                    if(entity==null && level.getChunkSource().hasChunk(pos.getX()>>4,pos.getZ()>>4)){
                        int count=missing.merge(slot.uuid(),1,Integer::sum);if(count>=5){ledger.put(new Run(run.uuid(),run.zone(),run.wave(),Phase.REVIEW,0,run.spawns(),run.players()));ZianManager.LOGGER.warn("Encounter {} paused: mob {} missing; cancel explicitly to reset",zone.id(),slot.uuid());}
                    }else if(entity!=null){missing.remove(slot.uuid());if(!zone.contains(zone.dimension(),entity.getX(),entity.getY(),entity.getZ()))entity.teleportTo(point.x(),point.y(),point.z());var spec=store.data().mobs().get(slot.template());if(spec.boss() && entity instanceof LivingEntity living){var bar=bars.computeIfAbsent(slot.uuid(),uuid->new ServerBossEvent(Component.literal(spec.name()),BossEvent.BossBarColor.YELLOW,BossEvent.BossBarOverlay.PROGRESS));bar.setProgress(living.getHealth()/living.getMaxHealth());bar.removeAllPlayers();for(UUID uuid:players){var player=server.getPlayerList().getPlayer(uuid);if(player!=null)bar.addPlayer(player);}}}
                }
            }
            bars.entrySet().removeIf(e->{boolean live=false;for(var level:server.getAllLevels())if(level.getEntity(e.getKey()) instanceof LivingEntity entity && entity.isAlive())live=true;if(!live)e.getValue().removeAllPlayers();return !live;});
        }catch(Exception error){ZianManager.LOGGER.error("Dungeon update rejected; no automatic reward retry",error);}
    }
}
