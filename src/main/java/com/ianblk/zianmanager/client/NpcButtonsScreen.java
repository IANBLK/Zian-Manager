package com.ianblk.zianmanager.client;
import com.google.gson.*;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import java.util.*;
import java.util.function.Consumer;
public final class NpcButtonsScreen extends ManagerThemedScreen {
 private final Screen parent;private final Consumer<String> accept;private final List<JsonObject> buttons=new ArrayList<>();private int left,top,w,h,page;
 public NpcButtonsScreen(Screen parent,String before,Consumer<String> accept){super(Component.literal("Botones adicionales del NPC"));this.parent=parent;this.accept=accept;for(var entry:JsonParser.parseString(before).getAsJsonArray())buttons.add(entry.getAsJsonObject().deepCopy());}
 @Override protected void init(){w=Math.min(390,width-24);h=Math.min(278,height-24);left=(width-w)/2;top=(height-h)/2;int per=Math.max(1,(h-116)/28),pages=Math.max(1,(buttons.size()+per-1)/per);page=Math.min(page,pages-1);
  for(int i=page*per;i<Math.min(buttons.size(),(page+1)*per);i++){int at=i,y=top+58+(i-page*per)*28;var button=buttons.get(i);addRenderableWidget(themed(left+12,y,w-90,22,Component.literal(button.get("label").getAsString()),b->edit(at)));addRenderableWidget(themed(left+w-72,y,60,22,Component.literal("Quitar"),b->{buttons.remove(at);rebuildWidgets();}));}
  addRenderableWidget(themed(left+12,top+h-54,30,20,Component.literal("<"),b->{page=Math.max(0,page-1);rebuildWidgets();}));addRenderableWidget(themed(left+48,top+h-54,30,20,Component.literal(">"),b->{page=Math.min(pages-1,page+1);rebuildWidgets();}));var add=addRenderableWidget(themed(left+84,top+h-54,w-96,20,Component.literal("Añadir botón"),b->edit(-1)));add.active=buttons.size()<7;addRenderableWidget(themed(left+12,top+h-28,(w-30)/2,20,Component.literal("Aplicar botones"),b->{var array=new JsonArray();buttons.forEach(array::add);accept.accept(array.toString());}));addRenderableWidget(themed(left+18+(w-30)/2,top+h-28,(w-30)/2,20,Component.literal("Cancelar"),b->onClose()));
 }
 private void edit(int at){JsonObject old=at<0?null:buttons.get(at);minecraft.setScreen(new NpcButtonEditor(this,old,v->{if(at<0)buttons.add(v);else buttons.set(at,v);minecraft.setScreen(this);}));}
 @Override public void onClose(){minecraft.setScreen(parent);}
 @Override public void render(GuiGraphics g,int mx,int my,float dt){panel(g,left,top,w,h);g.drawString(font,"Hasta 7 adicionales; el principal se edita en Acción.",left+12,top+36,0xFFAAAAAA);super.render(g,mx,my,dt);}
}
