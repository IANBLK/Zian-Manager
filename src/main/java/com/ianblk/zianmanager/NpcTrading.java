package com.ianblk.zianmanager;

import com.google.gson.*;
import com.ianblk.zianmanager.core.AtomicJson;
import com.ianblk.zianmanager.core.NpcTradeData;
import com.ianblk.zianmanager.core.NpcTradeData.*;
import com.ianblk.zianmanager.permission.ManagerPermissions;
import com.ianblk.zianmanager.reward.LootService;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.*;
import net.minecraft.core.component.DataComponentPredicate;
import java.nio.file.Path;
import java.util.*;

/** Item snapshots come exclusively from the administrator's server inventory. */
public final class NpcTrading {
    public record Stacks(List<ItemStack> payments,List<ItemStack> results) {
        public Stacks {payments=payments.stream().map(ItemStack::copy).toList();results=results.stream().map(ItemStack::copy).toList();}
    }
    private record Edit(UUID token,UUID npc,long until,long revision,Offer draft){}
    private static NpcTrading instance;
    private final Path file;
    private Data data;
    private final Map<UUID,Edit> edits=new HashMap<>();
    private final Map<UUID,Long> revisions=new HashMap<>();
    public NpcTrading(Path file)throws java.io.IOException {
        this.file=file;try{var raw=AtomicJson.read(file,JsonObject.class,null);data=raw==null?new Data(2,Map.of()):NpcTradeData.decode(raw);if(raw!=null&&raw.get("schema").getAsInt()==1){AtomicJson.write(file.resolveSibling("npc_trades-v1-backup-"+System.currentTimeMillis()+".json"),raw);AtomicJson.write(file,data);}instance=this;}catch(RuntimeException error){throw new java.io.IOException("Comercios ilegibles; archivo conservado",error);}
    }
    public static NpcTrading get(){return Objects.requireNonNull(instance,"Comercios no disponibles");}
    public boolean has(UUID npc){return !data.npcs().getOrDefault(npc,List.of()).isEmpty();}
    public long revision(UUID npc){return revisions.getOrDefault(npc,0L);}
    public static boolean allowed(ServerPlayer p){return ManagerPermissions.allows(p.createCommandSourceStack(),"npc",false)&&ManagerPermissions.allows(p.createCommandSourceStack(),"trade",false);}
    private static void admin(ServerPlayer p){if(!ManagerPermissions.allows(p.createCommandSourceStack(),"admin",true))throw new IllegalArgumentException("Necesitas zianmanager.admin");}
    private static ManagerNpcs.DialogueNpc entity(ServerPlayer p,UUID id){var spec=ManagerRuntime.get().store().data().npcs().get(id);var e=p.serverLevel().getEntity(id);if(spec==null||!(e instanceof ManagerNpcs.DialogueNpc npc)||!npc.isAlive()||npc.distanceToSqr(p)>36)throw new IllegalArgumentException("Acércate al NPC para configurar o comerciar");return npc;}
    public void open(ServerPlayer p,UUID id){
        if(!allowed(p)||p.isSpectator())throw new IllegalArgumentException("No tienes permiso para comerciar");
        var npc=entity(p,id);if(!has(id))throw new IllegalArgumentException("Este NPC no tiene ofertas");
        var rows=rows(p,id);var labels=data.npcs().getOrDefault(id,List.of()).stream().map(row->shortLabel(p,row)).toList();p.closeContainer();
        p.openMenu(new SimpleMenuProvider((container,inventory,player)->new NpcTradeMenu(container,inventory,npc,revision(id),rows,labels),npc.getDisplayName()),buffer->{buffer.writeVarInt(labels.size());for(String label:labels)buffer.writeUtf(label,96);});
    }
    public List<Stacks> rows(ServerPlayer p,UUID id){return data.npcs().getOrDefault(id,List.of()).stream().map(row->new Stacks(row.payments().stream().map(text->read(p,text)).toList(),row.results().stream().map(text->read(p,text)).toList())).toList();}
    public String shortLabel(ServerPlayer p,Offer row){String text=label(p,row.payments().getFirst())+(row.payments().size()>1?" +"+(row.payments().size()-1):"")+" → "+label(p,row.results().getFirst())+(row.results().size()>1?" +"+(row.results().size()-1):"");return text.length()>96?text.substring(0,95)+"…":text;}
    private static ItemStack read(ServerPlayer p,String text){var stack=LootService.item(p,text);check(stack);return stack;}
    private static void check(ItemStack s){if(s.isEmpty()||s.getCount()<1||s.getCount()>s.getMaxStackSize())throw new IllegalArgumentException("Usa una pila válida, entre 1 y el máximo del objeto");}
    private static String snapshot(ServerPlayer p,ItemStack s){check(s);String encoded=s.save(p.registryAccess()).toString();if(encoded.length()>32768)throw new IllegalArgumentException("Objeto demasiado grande");return encoded;}
    private static String label(ServerPlayer p,String text){if(text.isEmpty())return "ninguno";var s=read(p,text);String name=s.getHoverName().getString().replaceAll("[\\p{Cntrl}]"," ");if(name.length()>96)name=name.substring(0,96)+"…";return s.getCount()+" × "+name;}
    private String label(ServerPlayer p,Offer row){return row.payments().stream().map(text->label(p,text)).collect(java.util.stream.Collectors.joining(" + "))+" → "+row.results().stream().map(text->label(p,text)).collect(java.util.stream.Collectors.joining(" + "));}
    public void edit(ServerPlayer p,UUID id){admin(p);entity(p,id);show(p,id,null,"");}
    private void show(ServerPlayer p,UUID npc,Offer draft,String notice){
        edits.entrySet().removeIf(e->e.getValue().until()<System.currentTimeMillis());var token=UUID.randomUUID();edits.put(p.getUUID(),new Edit(token,npc,System.currentTimeMillis()+300000,revision(npc),draft));
        var out=new JsonObject();out.addProperty("type","npc_trades");out.addProperty("token",token.toString());out.addProperty("title","Comercio · "+entity(p,npc).getDisplayName().getString());out.addProperty("notice",notice);out.addProperty("draft",draft==null?"":label(p,draft));var rows=new JsonArray();for(var row:data.npcs().getOrDefault(npc,List.of()))rows.add(label(p,row));out.add("offers",rows);var previews=new JsonArray();for(int i=0;i<8;i++){var icon=new JsonObject();String text=draft==null?"":i<4?(i<draft.payments().size()?draft.payments().get(i):""):(i-4<draft.results().size()?draft.results().get(i-4):"");if(!text.isEmpty()){var stack=read(p,text);icon.addProperty("id",net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(stack.getItem()).toString());icon.addProperty("count",stack.getCount());icon.addProperty("name",label(p,text));}previews.add(icon);}out.add("draftSlots",previews);ManagerNetwork.send(p,out.toString());
    }
    public static boolean handles(ServerPlayer p,UUID token){return instance!=null&&instance.edits.containsKey(p.getUUID())&&instance.edits.get(p.getUUID()).token().equals(token);}
    public void action(ServerPlayer p,ManagerNetwork.Action action){
        var edit=edits.get(p.getUUID());if(edit==null||!edit.token().equals(action.token())||edit.until()<System.currentTimeMillis())return;edits.remove(p.getUUID());
        try{admin(p);entity(p,edit.npc());if(edit.revision()!=revision(edit.npc()))throw new IllegalArgumentException("Otro administrador cambió las ofertas; revisa la lista actualizada");var rows=new ArrayList<>(data.npcs().getOrDefault(edit.npc(),List.of()));
            if(action.operation().equals("trade_prepare")){var payments=new ArrayList<String>();var results=new ArrayList<String>();for(int i=0;i<4;i++){var a=p.getInventory().getItem(i);var b=p.getInventory().getItem(i+5);if(!a.isEmpty())payments.add(snapshot(p,a));if(!b.isEmpty())results.add(snapshot(p,b));}var draft=new Offer(payments,results);show(p,edit.npc(),draft,"Revisa las pilas antes de guardar; la ranura 5 se ignora");return;}
            if(action.operation().equals("trade_add")){if(edit.draft()==null||rows.size()>=64)throw new IllegalArgumentException("Prepara una oferta; máximo 64 por NPC");rows.add(edit.draft());}
            else if(action.operation().equals("trade_remove")){int index=JsonParser.parseString(action.json()).getAsJsonObject().get("index").getAsInt();if(index<0||index>=rows.size())throw new IllegalArgumentException("Oferta inexistente");rows.remove(index);}
            else if(action.operation().equals("trade_refresh")){show(p,edit.npc(),edit.draft(),"");return;}
            else throw new IllegalArgumentException("Acción inválida");
            var next=new LinkedHashMap<>(data.npcs());if(rows.isEmpty())next.remove(edit.npc());else next.put(edit.npc(),rows);var saved=new Data(2,next);String encoded=AtomicJson.GSON.toJson(saved);if(encoded.getBytes(java.nio.charset.StandardCharsets.UTF_8).length>16*1024*1024)throw new IllegalArgumentException("Archivo de comercios demasiado grande; reduce los datos de las ofertas");AtomicJson.writeEncoded(file,encoded);data=saved;revisions.merge(edit.npc(),1L,Long::sum);
            // Close a live trade before stale result packets can be processed.
            for(var viewer:p.getServer().getPlayerList().getPlayers())if(viewer.containerMenu instanceof NpcTradeMenu menu&&edit.npc().equals(menu.npcId()))viewer.closeContainer();
            show(p,edit.npc(),null,"Ofertas guardadas · intercambios ilimitados");
        }catch(Exception error){p.sendSystemMessage(Messages.format(Component.literal(error.getMessage()==null?"No se pudo guardar":error.getMessage())));try{show(p,edit.npc(),edit.draft(),error.getMessage());}catch(Exception ignored){}}
    }
}
