package com.ianblk.zianmanager.client;
import com.google.gson.*;
import com.ianblk.zianmanager.ManagerNetwork;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.*;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.network.PacketDistributor;
import java.util.*;
public final class ManagerScreen extends ManagerThemedScreen {
 private static final Map<String,String> LAST_TAB=new HashMap<>();
 private final JsonObject data;private final String type,id;private final UUID token;
 private final LinkedHashMap<String,String> values=new LinkedHashMap<>();private final Map<String,EditBox> inputs=new LinkedHashMap<>();
 private final Map<String,int[]> labels=new LinkedHashMap<>();private String tab;private int page,left,top,w,h;
 public ManagerScreen(JsonObject data){super(Component.literal(EditorSections.title(data.get("type").getAsString())));this.data=data;type=data.get("type").getAsString();id=data.get("id").getAsString();token=UUID.fromString(data.get("token").getAsString());if(data.has("fields"))data.getAsJsonObject("fields").entrySet().forEach(e->values.put(e.getKey(),e.getValue().getAsString()));tab=LAST_TAB.getOrDefault(type,EditorSections.groups(type).keySet().stream().findFirst().orElse(""));}
 private boolean list(){return type.equals("root") || type.endsWith("s") || type.equals("lootlist") || type.equals("claim");}
 @Override protected void init(){w=Math.min(430,width-24);h=Math.min(294,height-16);left=(width-w)/2;top=(height-h)/2;inputs.clear();labels.clear();
  if(list()){listWidgets();return;}
  var groups=EditorSections.groups(type);if(!groups.containsKey(tab))tab=groups.keySet().iterator().next();int i=0,tw=(w-24)/Math.max(1,groups.size());
  for(String name:groups.keySet()){var b=button(left+12+i++*tw,top+32,tw-3,name,()->{tab=name;LAST_TAB.put(type,tab);page=0;rebuildWidgets();});b.active=!name.equals(tab);}
  List<String> keys=groups.get(tab);int pages=Math.max(1,(keys.size()+3)/4);page=Math.min(page,pages-1);
  int col=(w-36)/2;
  for(i=page*4;i<Math.min(keys.size(),(page+1)*4);i++){String key=keys.get(i);int index=i-page*4;field(key,left+12+(index%2)*(col+12),top+98+(index/2)*42,col);}
  if(type.equals("npc") && tab.equals("Diálogo")){var box=new MultiLineEditBox(font,left+12,top+98,w-24,Math.max(40,h-172),Component.literal("Escribe lo que dirá el personaje…"),Component.literal("Diálogo"));box.setCharacterLimit(1024);box.setValue(values.getOrDefault("text","").replace("\\n","\n"));box.setValueListener(v->values.put("text",v));addRenderableWidget(box);}
  if(type.equals("mob") && tab.equals("Efectos"))button(left+12,top+98,w-24,"Elegir y configurar efectos",()->minecraft.setScreen(new EffectsScreen(this,values.getOrDefault("effects",""),v->{values.put("effects",v);minecraft.setScreen(this);})));
  var actions=new ArrayList<Runnable>();var names=new ArrayList<String>();
  if(type.equals("zone") && tab.equals("Ubicación")){action(names,actions,"Marcar esquina 1",()->send("first"));action(names,actions,"Marcar esquina 2",()->send("second"));action(names,actions,"Añadir aparición aquí",()->send("point"));action(names,actions,"Borrar apariciones",()->confirm("¿Borrar puntos?","Tendrás que volver a colocar los puntos de aparición.","clear_points"));}
  else{action(names,actions,"Guardar cambios",()->send("save"));
   if(type.equals("mob") && tab.equals("Equipo"))action(names,actions,"Copiar objeto de mano",()->send("equip"));
   if(type.equals("mob") && (tab.equals("Básico") || tab.equals("Equipo")))action(names,actions,"Ver mob de prueba",()->send("preview"));
   if(type.equals("loot") && tab.equals("Objetos")){action(names,actions,"Añadir objeto de mano",()->send("add"));action(names,actions,"Editar seleccionado",()->send("edit_entry"));}
   if(type.equals("zone") && tab.equals("Oleadas")){action(names,actions,"Probar encuentro",()->send("test"));action(names,actions,"Detener encuentro",()->send("cancel"));}
   if(type.equals("npc") && tab.equals("Apariencia"))action(names,actions,"Mover a mi posición",()->send("move"));
   if(type.equals("npc") && tab.equals("Acción") && !id.isEmpty())action(names,actions,"Restablecer usos",()->confirm("¿Restablecer usos?","Borra esperas y revisiones. Comprueba antes las entregas anteriores.","reset_npc_actions"));
  }
  int count=names.size(),cols=Math.min(3,count),aw=(w-24-(cols-1)*6)/cols,ay=top+h-58-(count>3?24:0);
  for(i=0;i<count;i++){int index=i;button(left+12+(i%cols)*(aw+6),ay+(i/cols)*24,aw,names.get(i),actions.get(index));}
  if(type.equals("loot") && tab.equals("Objetos")){button(left+12,top+75,w-114,"Seleccionar objeto de la tabla",this::selectEntry);button(left+w-96,top+75,84,"Quitar objeto",()->confirm("¿Quitar objeto?","Se eliminará solo la entrada seleccionada.","remove_entry"));}
  if(!id.isEmpty() || type.equals("chest"))button(left+12,top+h-28,86,"Eliminar…",()->confirm("¿Eliminar?","Sus registros de entregas se conservan.","delete_confirmed"));
  if(pages>1){button(left+w/2-43,top+h-28,35,"<",()->{page=Math.max(0,page-1);rebuildWidgets();}).active=page>0;button(left+w/2+8,top+h-28,35,">",()->{page=Math.min(pages-1,page+1);rebuildWidgets();}).active=page+1<pages;}
  button(left+w-88,top+h-28,76,"Volver",this::back);
 }
 private void field(String key,int x,int y,int size){
  if(key.equals("text") && type.equals("npc"))return;
  labels.put(key,new int[]{x,y-11,size});
  if(key.equals("entity")){button(x,y,size,pretty(values.get(key)),()->minecraft.setScreen(new EntitySelectorScreen(this,v->{values.put(key,v);minecraft.setScreen(this);})));return;}
  if(key.equals("loot") || key.equals("completionLoot")){button(x,y,size,values.getOrDefault(key,"").isBlank()?"Sin recompensa":pretty(values.get(key)),()->choose("loot","Elegir recompensa",true,v->values.put(key,v)));return;}
  if(key.equals("team")){button(x,y,size,teamCount()+" mobs · Elegir equipo",()->minecraft.setScreen(new TeamScreen(this,choices("mobs"),values.getOrDefault("team",""),v->{values.put("team",v);minecraft.setScreen(this);})));return;}
  if(Set.of("boss","enabled","category","slot","skin").contains(key)){
   var options=new JsonArray();List<String> list=key.equals("skin")?com.ianblk.zianmanager.core.Definitions.NPC_SKINS:key.equals("category")?List.of("MOB","BOSS","CHEST"):key.equals("slot")?List.of("mainhand","offhand","head","chest","legs","feet"):List.of("false","true");
   for(String v:list){var o=new JsonObject();o.addProperty("id",v);o.addProperty("name",pretty(v));options.add(o);}
   button(x,y,size,pretty(values.get(key)),()->minecraft.setScreen(new ChoiceScreen(this,label(key),options,false,v->{values.put(key,v);minecraft.setScreen(this);})));return;
  }
  var box=new EditBox(font,x,y,size,20,Component.literal(label(key)));box.setMaxLength(key.equals("command")?2048:key.equals("effects")?512:128);box.setValue(values.getOrDefault(key,""));box.setResponder(v->values.put(key,v));box.setTooltip(Tooltip.create(Component.literal(helpField(key))));if(key.equals("id") && !id.isEmpty())box.setEditable(false);inputs.put(key,box);addRenderableWidget(box);
 }
 private void listWidgets(){var entries=data.getAsJsonArray("entries");int per=Math.max(1,(h-86)/26),pages=Math.max(1,(entries.size()+per-1)/per);page=Math.min(page,pages-1);
  for(int i=page*per;i<Math.min(entries.size(),(page+1)*per);i++){var e=entries.get(i).getAsJsonObject();String target=e.has("id")?e.get("id").getAsString():"";String text=type.equals("root")?EditorSections.title(target.equals("loot")?"lootlist":target):e.get("name").getAsString();var b=button(left+12,top+62+(i-page*per)*26,w-24,text,()->{if(type.equals("claim"))return;request(type.equals("root")?(target.equals("loot")?"lootlist":target):type.equals("lootlist")?"loot":type.substring(0,type.length()-1),type.equals("root")?"":target);});if(type.equals("claim"))b.active=false;}
  button(left+12,top+h-28,30,"<",()->{page=Math.max(0,page-1);rebuildWidgets();}).active=page>0;button(left+46,top+h-28,30,">",()->{page=Math.min(pages-1,page+1);rebuildWidgets();}).active=page+1<pages;
  if(type.equals("chests"))button(left+84,top+h-28,w-184,"Obtener cofre",()->{if(minecraft.getConnection()!=null)minecraft.getConnection().sendCommand("zianmanager givechest");onClose();});
  if(type.equals("claim"))button(left+84,top+h-28,w-184,"Recibir recompensas",()->send("claim"));else if(!type.equals("root") && !type.equals("chests"))button(left+84,top+h-28,w-184,"Crear nuevo",()->{LAST_TAB.remove(type.equals("lootlist")?"loot":type.substring(0,type.length()-1));request(type.equals("lootlist")?"loot":type.substring(0,type.length()-1),"");});
  button(left+w-88,top+h-28,76,type.equals("root")?"Cerrar":"Volver",this::back);
 }
 private void selectEntry(){minecraft.setScreen(new EntriesScreen(this,data.getAsJsonArray("entries"),index->{values.put("entry",index);for(var e:data.getAsJsonArray("entries")){var o=e.getAsJsonObject();if(o.get("id").getAsString().equals(index))for(String k:List.of("weight","min","max"))if(o.has(k))values.put(k,o.get(k).getAsString());}minecraft.setScreen(this);}));}
 private JsonArray choices(String kind){return data.has("choices")?data.getAsJsonObject("choices").getAsJsonArray(kind):new JsonArray();}
 private void choose(String kind,String title,boolean none,java.util.function.Consumer<String> accept){minecraft.setScreen(new ChoiceScreen(this,title,choices(kind),none,v->{accept.accept(v);minecraft.setScreen(this);}));}
 private int teamCount(){return (int)Arrays.stream(values.getOrDefault("team","").split(",")).filter(v->!v.isBlank()).count();}
 private static void action(List<String> names,List<Runnable> actions,String name,Runnable action){names.add(name);actions.add(action);}
 private void confirm(String title,String message,String operation){minecraft.setScreen(new ManagerConfirmScreen(ok->{if(ok)send(operation);else minecraft.setScreen(this);},Component.literal(title),Component.literal(message)));}
 private void back(){if(type.equals("root")){onClose();return;}request(list()?"root":type.equals("loot")?"lootlist":type+"s","");}
 private void request(String section,String target){PacketDistributor.sendToServer(new ManagerNetwork.Request(section,target));}
 private void send(String operation){var fields=new JsonObject();values.forEach(fields::addProperty);PacketDistributor.sendToServer(new ManagerNetwork.Action(token,operation,fields.toString()));}
 private Button button(int x,int y,int size,String text,Runnable action){return addRenderableWidget(themed(x,y,size,20,Component.literal(text),b->action.run()));}
 private static String pretty(String value){if(value==null)return "";return switch(value){case "true"->"Sí";case "false"->"No";case "MOB"->"Mobs";case "BOSS"->"Jefes";case "CHEST"->"Cofres";case "mainhand"->"Mano principal";case "offhand"->"Mano secundaria";case "head"->"Cabeza";case "chest"->"Pecho";case "legs"->"Piernas";case "feet"->"Pies";default->value.replace("minecraft:","").replace("_"," ");};}
 private static String label(String key){return switch(key){case "id"->"Nombre / referencia";case "name"->"Nombre visible";case "entity"->"Tipo de mob";case "health"->"Vida máxima";case "damage"->"Daño de ataque";case "armor"->"Armadura";case "toughness"->"Dureza de armadura";case "knockback"->"Resistencia al empuje (0–1)";case "boss"->"¿Es un jefe?";case "category"->"Usar para";case "rolls"->"Cuántos objetos sortear";case "weight"->"Probabilidad relativa";case "min"->"Cantidad mínima";case "max"->"Cantidad máxima";case "table"->"Tabla externa (opcional)";case "team"->"Mobs de cada oleada";case "waves"->"Número de oleadas";case "pauseSeconds"->"Pausa entre oleadas (seg)";case "respawnSeconds"->"Volver a aparecer (seg)";case "enabled"->"¿Activar la zona?";case "minutes"->"Renovar loot (minutos)";case "effects"->"Formato manual de efectos";case "skin"->"Apariencia";case "button"->"Texto del botón";case "command"->"Comando del botón";case "cooldownSeconds"->"Espera por jugador (seg)";case "slot"->"Equipar en";case "loot","completionLoot"->"Recompensas";default->key;};}
 private static String helpField(String key){return switch(key){case "id"->"Se crea una referencia automáticamente. Puedes cambiarla antes de guardar: minúsculas, números y _.";case "weight"->"Más peso significa más probabilidad. El porcentaje se muestra en la lista de objetos.";case "command"->"Usa {player} para el nombre del jugador. Ejemplo: minecraft:tp {player} 100 70 100";case "effects"->"Opcional. El selector de Efectos permite configurarlos sin escribir este formato.";default->label(key);};}
 private String help(){if(list())return type.equals("root")?"Elige qué quieres configurar.":type.equals("chests")?"Coloca un cofre y usa Shift + clic derecho para configurarlo.":"Selecciona uno para editarlo o crea uno nuevo.";
  if(type.equals("loot"))return tab.equals("Objetos")?"Sostén un objeto para añadirlo; elige uno de la lista para editarlo.":tab.equals("Avanzado")?"Opcional: usa una tabla externa en lugar de objetos propios.":"Elige el tipo de recompensa y cuántos objetos se sortean.";
  if(type.equals("zone"))return tab.equals("Ubicación")?"Marca las esquinas y un punto libre por cada mob desde tu posición.":tab.equals("General")?"Selecciona de 3 a 6 mobs; activa la zona cuando esté preparada.":"Define las oleadas, las pausas y el tiempo para repetir la dungeon.";
  if(type.equals("mob"))return tab.equals("Equipo")?"Sostén un objeto, elige la ranura y pulsa Copiar objeto de mano.":tab.equals("Avanzado")?"Opcional. Los ajustes habituales están en las otras secciones.":tab.equals("Efectos")?"Selecciona efectos, su nivel y cuánto duran, sin escribir IDs.":"Configura esta sección y guarda. Puedes volver a editar después.";
  if(type.equals("npc"))return tab.equals("Acción")?"Opcional: un botón ejecuta tu comando. {player} es el jugador.":tab.equals("Diálogo")?"Escribe el diálogo; puedes usar varias líneas.":"Elige nombre y skin; el personaje aparece en tu posición.";
  return "Selecciona sus recompensas y cada cuánto se renuevan.";
 }
 @Override public void render(GuiGraphics g,int mx,int my,float dt){panel(g,left,top,w,h);g.drawString(font,font.plainSubstrByWidth(help(),w-24),left+12,top+(list()?38:61),0xFFAAAAAA);
  if(!list() && !(type.equals("loot") && tab.equals("Objetos"))){String notice=data.has("notice")?data.get("notice").getAsString():"";g.drawString(font,font.plainSubstrByWidth(notice,w-24),left+12,top+76,0xFFF7E2AC);}
  for(var e:labels.entrySet()){var pos=e.getValue();g.drawString(font,font.plainSubstrByWidth(label(e.getKey()),pos[2]),pos[0],pos[1],0xFFDDDDDD);}super.render(g,mx,my,dt);
 }
}
