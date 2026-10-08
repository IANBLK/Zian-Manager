package com.ianblk.zianmanager.client;
import com.google.gson.*;
import com.ianblk.zianmanager.ManagerNetwork;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.*;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.network.PacketDistributor;
import java.util.*;
public final class ManagerScreen extends Screen {
    private final JsonObject data;private final String type,id;private final UUID token;
    private final LinkedHashMap<String,String> values=new LinkedHashMap<>();private final Map<String,EditBox> inputs=new LinkedHashMap<>();
    private int page,left,top,panelWidth;
    public ManagerScreen(JsonObject data){super(Component.literal(data.get("title").getAsString()));this.data=data;type=data.get("type").getAsString();id=data.get("id").getAsString();token=UUID.fromString(data.get("token").getAsString());if(data.has("fields"))data.getAsJsonObject("fields").entrySet().forEach(e->values.put(e.getKey(),e.getValue().getAsString()));}
    @Override public boolean isPauseScreen(){return false;}
    private boolean list(){return type.equals("root") || type.endsWith("s") || type.equals("lootlist") || type.equals("claim");}
    @Override protected void init(){
        panelWidth=Math.min(430,width-20);left=(width-panelWidth)/2;top=8;
        inputs.clear();int bottom=height-30;
        if(list()){
            var entries=data.getAsJsonArray("entries");int per=Math.max(1,(height-110)/24);int pages=Math.max(1,(entries.size()+per-1)/per);page=Math.min(page,pages-1);
            for(int i=page*per;i<Math.min(entries.size(),(page+1)*per);i++){var entry=entries.get(i).getAsJsonObject();String name=entry.get("name").getAsString();String target=entry.has("id")?entry.get("id").getAsString():"";
                var button=button(left+8,top+48+(i-page*per)*24,panelWidth-16,name,()->{
                    if(type.equals("claim"))return;
                    String section=type.equals("root")?(target.equals("loot")?"lootlist":target):type.equals("lootlist")?"loot":type.substring(0,type.length()-1);
                    request(section,type.equals("root")?"":target);
                });if(type.equals("claim"))button.active=false;
            }
            button(left+8,bottom,32,"<",()->{page=Math.max(0,page-1);rebuildWidgets();}).active=page>0;
            button(left+44,bottom,32,">",()->{page++;rebuildWidgets();}).active=page+1<pages;
            if(type.equals("claim"))button(left+84,bottom,panelWidth-160,"Recibir todo",()->send("claim"));
            else if(!type.equals("root"))button(left+84,bottom,panelWidth-160,"Crear",()->request(type.equals("lootlist")?"loot":type.substring(0,type.length()-1),""));
        }else{
            List<String> keys=new ArrayList<>(values.keySet());int per=6;int pages=Math.max(1,(keys.size()+per-1)/per);page=Math.min(page,pages-1);
            int boxWidth=(panelWidth-28)/2;int rowStep=Math.min(41,Math.max(32,(height-145)/3));
            for(int i=page*per;i<Math.min(keys.size(),(page+1)*per);i++){
                String key=keys.get(i);int index=i-page*per;int x=left+8+(index%2)*(boxWidth+12),y=top+63+(index/2)*rowStep;
                if(Set.of("boss","enabled","category","slot").contains(key)){
                    List<String> choices=key.equals("category")?List.of("MOB","BOSS","CHEST"):key.equals("slot")?List.of("mainhand","offhand","head","chest","legs","feet"):List.of("false","true");
                    button(x,y,boxWidth,label(key)+": "+values.get(key),()->{int at=choices.indexOf(values.get(key));values.put(key,choices.get((at+1)%choices.size()));rebuildWidgets();});continue;
                }
                var box=new EditBox(font,x,y,boxWidth,20,Component.literal(label(key)));box.setMaxLength(key.equals("text")?1024:key.equals("effects")?512:key.equals("team")?256:128);box.setValue(values.get(key));box.setResponder(v->values.put(key,v));inputs.put(key,box);addRenderableWidget(box);
            }
            int y=Math.min(top+196,height-76);int bw=Math.max(46,(panelWidth-24)/4);
            button(left+8,y,bw,"Guardar",()->send("save"));
            if(type.equals("mob")){button(left+12+bw,y,bw,"Equipo mano",()->send("equip"));button(left+16+bw*2,y,bw,"Vista previa",()->send("preview"));button(left+20+bw*3,y,bw,"Elegir entidad",()->minecraft.setScreen(new EntitySelectorScreen(this,v->{values.put("entity",v);minecraft.setScreen(this);})));}
            if(type.equals("loot")){button(left+12+bw,y,bw,"Añadir mano",()->send("add"));button(left+16+bw*2,y,bw,"Editar entrada",()->send("edit_entry"));button(left+20+bw*3,y,bw,"Quitar entrada",()->send("remove_entry"));}
            if(type.equals("zone")){
                button(left+12+bw,y,bw,"Esquina 1",()->send("first"));button(left+16+bw*2,y,bw,"Esquina 2",()->send("second"));button(left+20+bw*3,y,bw,"Añadir punto",()->send("point"));
                button(left+8,y+24,bw,"Borrar puntos",()->send("clear_points"));button(left+12+bw,y+24,bw,"Probar",()->send("test"));button(left+16+bw*2,y+24,bw,"Cancelar run",()->send("cancel"));
            }
            if(type.equals("npc"))button(left+12+bw,y,bw,"Mover aquí",()->send("move"));
            if(!id.isEmpty() || type.equals("chest"))button(left+8,bottom,106,"Eliminar…",()->minecraft.setScreen(new ConfirmScreen(ok->{if(ok)send("delete_confirmed");else minecraft.setScreen(this);},Component.literal("¿Eliminar "+type+"?"),Component.literal("Las recompensas y sus registros se conservan."))));
            if(pages>1){button(left+120,bottom,32,"<",()->{page=Math.max(0,page-1);rebuildWidgets();}).active=page>0;button(left+156,bottom,32,">",()->{page++;rebuildWidgets();}).active=page+1<pages;}
            if(type.equals("loot"))button(left+196,bottom,86,"Ver entradas",()->minecraft.setScreen(new EntriesScreen(this,data.getAsJsonArray("entries"),index->{values.put("entry",index);minecraft.setScreen(this);})));
        }
        button(left+panelWidth-66,bottom,58,"Volver",()->request("root",""));
    }
    private void request(String section,String id){PacketDistributor.sendToServer(new ManagerNetwork.Request(section,id));}
    private void send(String operation){var fields=new JsonObject();values.forEach(fields::addProperty);PacketDistributor.sendToServer(new ManagerNetwork.Action(token,operation,fields.toString()));}
    private Button button(int x,int y,int w,String text,Runnable action){return addRenderableWidget(Button.builder(Component.literal(text),b->action.run()).bounds(x,y,w,20).build());}
    private static String label(String key){return switch(key){case "entity"->"Entidad";case "health"->"Vida máxima";case "damage"->"Daño";case "armor"->"Armadura";case "toughness"->"Dureza";case "knockback"->"Resistencia empuje 0–1";case "boss"->"Jefe: true / false";case "category"->"MOB / BOSS / CHEST";case "rolls"->"Objetos seleccionados (1–32)";case "weight"->"Peso / probabilidad relativa";case "table"->"Loot table externa opcional";case "entry"->"Índice de entrada";case "team"->"3–6 IDs de mob, separados por ,";case "waves"->"Número de oleadas";case "pauseSeconds"->"Pausa entre oleadas (seg)";case "respawnSeconds"->"Reaparición (seg)";case "enabled"->"Habilitada: true / false";case "minutes"->"Renovación por jugador (min)";case "effects"->"Efectos: id,amplificador,seg;…";case "text"->"Texto del NPC (\\n = salto)";default->key;};}
    @Override public void render(GuiGraphics g,int mx,int my,float dt){
        g.fill(0,0,width,height,0xE0181818);g.fill(left,top,left+panelWidth,height-8,0xFF202020);g.fill(left,top,left+panelWidth,top+3,0xFFE0B14A);g.drawCenteredString(font,title,width/2,top+10,0xFFE0B14A);
        String notice=data.has("notice")?data.get("notice").getAsString():"";g.drawString(font,font.plainSubstrByWidth(notice,panelWidth-16),left+8,top+30,0xFFC5C5C5);
        if(!list())for(var e:inputs.entrySet())g.drawString(font,font.plainSubstrByWidth(label(e.getKey()),e.getValue().getWidth()),e.getValue().getX(),e.getValue().getY()-11,0xFFDDDDDD);
        super.render(g,mx,my,dt);
    }
}
