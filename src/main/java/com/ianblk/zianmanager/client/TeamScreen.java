package com.ianblk.zianmanager.client;
import com.google.gson.*;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import java.util.*;
import java.util.function.Consumer;
public final class TeamScreen extends ManagerThemedScreen {
 private final Screen parent;private final JsonArray choices;private final Consumer<String> accept;private final List<String> team=new ArrayList<>();private int left,top,w,h;
 public TeamScreen(Screen parent,JsonArray choices,String before,Consumer<String> accept){super(Component.literal("Mobs de cada oleada"));this.parent=parent;this.choices=choices;this.accept=accept;for(String id:before.split(","))if(!id.isBlank())team.add(id.trim());}
 @Override protected void init(){w=Math.min(390,width-24);h=Math.min(244,height-24);left=(width-w)/2;top=(height-h)/2;int col=(w-30)/2;
  for(int i=0;i<team.size();i++){int at=i;String name=team.get(i);for(var e:choices)if(e.getAsJsonObject().get("id").getAsString().equals(name)){name=e.getAsJsonObject().get("name").getAsString();break;}addRenderableWidget(themed(left+12+(i%2)*(col+6),top+64+(i/2)*35,col,24,Component.literal((i+1)+". "+name),b->select(at)));}
  var add=addRenderableWidget(themed(left+12,top+h-54,(w-30)/2,20,Component.literal("Añadir mob"),b->select(-1)));add.active=team.size()<6;var remove=addRenderableWidget(themed(left+18+(w-30)/2,top+h-54,(w-30)/2,20,Component.literal("Quitar último"),b->{team.removeLast();rebuildWidgets();}));remove.active=!team.isEmpty();addRenderableWidget(themed(left+12,top+h-28,(w-30)/2,20,Component.literal("Aplicar equipo"),b->accept.accept(String.join(",",team))));addRenderableWidget(themed(left+18+(w-30)/2,top+h-28,(w-30)/2,20,Component.literal("Cancelar"),b->onClose()));
 }
 private void select(int at){minecraft.setScreen(new ChoiceScreen(this,"Elegir mob o jefe",choices,false,id->{if(at<0)team.add(id);else team.set(at,id);minecraft.setScreen(this);}));}
 @Override public void onClose(){minecraft.setScreen(parent);}
 @Override public void render(GuiGraphics g,int mx,int my,float dt){panel(g,left,top,w,h);g.drawString(font,"Elige 3–6 mobs. Puedes repetir el mismo tipo.",left+12,top+36,0xFFAAAAAA);super.render(g,mx,my,dt);}
}
