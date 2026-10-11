package com.ianblk.zianmanager.client;
import com.google.gson.*;
import com.ianblk.zianmanager.ManagerNetwork;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.*;
import net.minecraft.network.chat.Component;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.*;
import net.neoforged.neoforge.network.PacketDistributor;
import java.util.*;
/** Visual prize rows and inline probability controls inspired by Zian Utilities' gacha editor. */
public final class LootEditorScreen extends ManagerThemedScreen {
 private static final Map<String,Integer> PAGES=new HashMap<>();private final JsonObject data;private final String id;private final UUID token;private final Map<String,String> values=new HashMap<>();private int page,left,top,w,h,per;private boolean sent;
 public LootEditorScreen(JsonObject data){super(Component.literal("Recompensas · Editor de loot"));this.data=data;id=data.get("id").getAsString();token=UUID.fromString(data.get("token").getAsString());data.getAsJsonObject("fields").entrySet().forEach(e->values.put(e.getKey(),e.getValue().getAsString()));page=PAGES.getOrDefault(id,0);}
 @Override protected void init(){w=Math.min(470,width-24);h=Math.min(330,height-16);left=(width-w)/2;top=(height-h)/2;var entries=data.getAsJsonArray("entries");per=Math.max(1,Math.min(4,(h-218)/30));int pages=Math.max(1,(entries.size()+per-1)/per);page=Math.min(page,pages-1);
  var name=new EditBox(font,left+12,top+43,w-104,20,Component.literal("Nombre de tabla"));name.setMaxLength(32);name.setValue(values.get("id"));name.setResponder(v->values.put("id",v));name.setEditable(id.isEmpty());addRenderableWidget(name);button(left+w-86,top+43,74,"Guardar",()->send("save"));
  button(left+12,top+69,90,category(),()->{var choices=new JsonArray();for(String v:independent()?List.of("MOB"):List.of("MOB","BOSS","CHEST")){var e=new JsonObject();e.addProperty("id",v);e.addProperty("name",v.equals("MOB")?"Mobs":v.equals("BOSS")?"Jefes":"Cofres");choices.add(e);}minecraft.setScreen(new ChoiceScreen(this,"Recompensas para…",choices,false,v->{values.put("category",v);minecraft.setScreen(this);}));});
  button(left+109,top+69,24,"−",()->changeRolls(-1)).active=!independent();button(left+137,top+69,24,"+",()->changeRolls(1)).active=!independent();button(left+w-105,top+69,93,"Opciones…",()->minecraft.setScreen(new LootValueScreen(this,true,values.getOrDefault("table",""),"",(a,b)->{values.put("table",a);send("save");}))).active=!independent();
  button(left+12,top+97,w-24,independent()?"Modo: porcentaje real · puede no soltar nada":"Modo: sorteo por peso · cantidad de objetos",()->{values.put("independent",""+!independent());if(independent()){values.put("table","");values.put("category","MOB");}if(id.isEmpty())rebuildWidgets();else send("save");}).active=values.get("category").equals("MOB");
  button(left+12,top+121,w-24,"Añadir objeto que tengo en la mano",()->{values.put("weight","10");values.put("min","1");int amount=minecraft.player==null?1:minecraft.player.getMainHandItem().getCount();values.put("max",""+Math.max(1,Math.min(64,amount)));send("add");});
  for(int i=page*per;i<Math.min(entries.size(),(page+1)*per);i++){var e=entries.get(i).getAsJsonObject();int y=top+150+(i-page*per)*30;addRenderableWidget(new PrizeCard(left+12,y,w-136,e,()->{select(e);minecraft.setScreen(new LootValueScreen(this,false,values.get("min"),values.get("max"),(a,b)->{values.put("min",a);values.put("max",b);send("edit_entry");}));}));
   button(left+w-118,y+3,24,"−",()->changeWeight(e,-1)).active=e.get("weight").getAsInt()>1;button(left+w-90,y+3,24,"+",()->changeWeight(e,1)).active=e.get("weight").getAsInt()<(independent()?100:10000);button(left+w-62,y+3,50,"Quitar",()->{select(e);minecraft.setScreen(new ManagerConfirmScreen(ok->{if(ok)send("remove_entry");else minecraft.setScreen(this);},Component.literal("¿Quitar este objeto?"),Component.literal(e.get("display").getAsString())));});
  }
  button(left+12,top+h-54,36,"<",()->{page=Math.max(0,page-1);PAGES.put(id,page);rebuildWidgets();}).active=page>0;button(left+w-48,top+h-54,36,">",()->{page=Math.min(pages-1,page+1);PAGES.put(id,page);rebuildWidgets();}).active=page+1<pages;
  if(!id.isEmpty())button(left+12,top+h-28,100,"Eliminar tabla",()->minecraft.setScreen(new ManagerConfirmScreen(ok->{if(ok)send("delete_confirmed");else minecraft.setScreen(this);},Component.literal("¿Eliminar tabla?"),Component.literal("No puedes eliminar una tabla usada por mobs, zonas o cofres."))));button(left+w-92,top+h-28,80,"Volver",()->PacketDistributor.sendToServer(new ManagerNetwork.Request("lootlist","")));
 }
 String selectedWeight(){return values.getOrDefault("weight","10");}
 void selectedWeight(String weight){values.put("weight",weight);}
 private void select(JsonObject e){values.put("entry",e.get("id").getAsString());for(String k:List.of("weight","min","max"))values.put(k,e.get(k).getAsString());}
 private void changeWeight(JsonObject e,int delta){select(e);values.put("weight",""+Math.max(1,Math.min(independent()?100:10000,e.get("weight").getAsInt()+delta)));send("edit_entry");}
 private void changeRolls(int delta){values.put("rolls",""+Math.max(1,Math.min(32,Integer.parseInt(values.get("rolls"))+delta)));if(id.isEmpty())rebuildWidgets();else send("save");}
 boolean independent(){return Boolean.parseBoolean(values.getOrDefault("independent","false"));}
 private String category(){return switch(values.get("category")){case "MOB"->"Mobs";case "BOSS"->"Jefes";default->"Cofres";};}
 private void send(String operation){if(sent)return;sent=true;PAGES.put(id,page);var fields=new JsonObject();values.forEach(fields::addProperty);PacketDistributor.sendToServer(new ManagerNetwork.Action(token,operation,fields.toString()));}
 private Button button(int x,int y,int size,String text,Runnable action){return addRenderableWidget(themed(x,y,size,20,Component.literal(text),b->action.run()));}
 private final class PrizeCard extends Button {
  private final JsonObject entry;private final ItemStack icon;
  PrizeCard(int x,int y,int size,JsonObject entry,Runnable action){super(x,y,size,26,Component.literal(entry.get("display").getAsString()),b->action.run(),DEFAULT_NARRATION);this.entry=entry;var key=ResourceLocation.tryParse(entry.get("icon").getAsString());icon=new ItemStack(key!=null && BuiltInRegistries.ITEM.containsKey(key)?BuiltInRegistries.ITEM.get(key):Items.BARRIER);setTooltip(Tooltip.create(Component.literal(entry.get("name").getAsString()+" · Clic para ajustar cantidades")));}
  @Override protected void renderWidget(GuiGraphics g,int mx,int my,float dt){int x=getX(),y=getY();g.fill(x,y,x+width,y+height,isHoveredOrFocused()?0xFF3A3326:0xFF242424);g.renderItem(icon,x+5,y+5);g.drawString(font,font.plainSubstrByWidth(getMessage().getString(),width-34),x+28,y+3,0xFFF4F7FA);int total=0;for(var e:data.getAsJsonArray("entries"))total+=e.getAsJsonObject().get("weight").getAsInt();String info=String.format(java.util.Locale.ROOT,"%.1f%%",independent()?entry.get("weight").getAsInt():100.0*entry.get("weight").getAsInt()/Math.max(1,total))+" · "+entry.get("min").getAsString()+"–"+entry.get("max").getAsString()+" unidades";g.drawString(font,font.plainSubstrByWidth(info,width-34),x+28,y+14,0xFF9FAAB5);}
 }
 @Override public void render(GuiGraphics g,int mx,int my,float dt){panel(g,left,top,w,h);g.drawString(font,"Nombre de tabla",left+12,top+31,0xFFAAAAAA);g.drawString(font,(independent()?"Cada objeto: % real":w>=400?values.get("rolls")+" objetos por entrega":"× "+values.get("rolls")),left+168,top+75,0xFFF7E2AC);int size=data.getAsJsonArray("entries").size();g.drawCenteredString(font,(page+1)+" / "+Math.max(1,(size+per-1)/per)+" · "+size+" objetos",width/2,top+h-48,0xFFAAAAAA);String notice=data.get("notice").getAsString();if(!notice.isBlank())g.drawString(font,font.plainSubstrByWidth(notice,w-24),left+12,top+h-68,0xFFF7E2AC);super.render(g,mx,my,dt);}
}
