package com.ianblk.zianmanager.client;
import com.google.gson.*;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import java.util.*;
import java.util.function.Consumer;
/** One title per configured world; a blank entry uses the default title. */
public final class TimerTitlesScreen extends ManagerThemedScreen {
 private final Screen parent;private final Consumer<String> accept;private final List<String> worlds;private final JsonObject titles;private int left,top,w,h,page;
 public TimerTitlesScreen(Screen parent,String worlds,String before,Consumer<String> accept){super(Component.literal("Nombres del temporizador por mundo"));this.parent=parent;this.accept=accept;this.worlds=Arrays.stream(worlds.split(",")).map(String::trim).filter(s->!s.isEmpty()).distinct().toList();titles=JsonParser.parseString(before).getAsJsonObject().deepCopy();}
 @Override protected void init(){w=Math.min(410,width-24);h=Math.min(280,height-24);left=(width-w)/2;top=(height-h)/2;int per=Math.max(1,(h-116)/42),pages=Math.max(1,(worlds.size()+per-1)/per);page=Math.min(page,pages-1);
  for(int i=page*per;i<Math.min(worlds.size(),(page+1)*per);i++){String world=worlds.get(i);int y=top+66+(i-page*per)*42;var box=new EditBox(font,left+12,y,w-24,20,Component.literal(world));box.setMaxLength(64);box.setValue(titles.has(world)?titles.get(world).getAsString():"");box.setHint(Component.literal("Vacío: usar título predeterminado"));box.setResponder(v->{if(v.isBlank())titles.remove(world);else titles.addProperty(world,v);});addRenderableWidget(box);}
  addRenderableWidget(themed(left+12,top+h-54,30,20,Component.literal("<"),b->{page=Math.max(0,page-1);rebuildWidgets();}));addRenderableWidget(themed(left+48,top+h-54,30,20,Component.literal(">"),b->{page=Math.min(pages-1,page+1);rebuildWidgets();}));addRenderableWidget(themed(left+84,top+h-54,w-96,20,Component.literal("Aplicar nombres"),b->accept.accept(titles.toString())));addRenderableWidget(themed(left+12,top+h-28,w-24,20,Component.literal("Cancelar"),b->onClose()));
 }
 @Override public void onClose(){minecraft.setScreen(parent);}
 @Override public void render(GuiGraphics g,int mx,int my,float dt){panel(g,left,top,w,h);g.drawString(font,worlds.isEmpty()?"Primero añade mundos en la pestaña Mundos.":"Cada dungeon muestra el nombre de su mundo.",left+12,top+36,0xFFAAAAAA);int per=Math.max(1,(h-116)/42);for(int i=page*per;i<Math.min(worlds.size(),(page+1)*per);i++)g.drawString(font,font.plainSubstrByWidth(worlds.get(i),w-24),left+12,top+54+(i-page*per)*42,0xFFF2CB68);super.render(g,mx,my,dt);}
}
