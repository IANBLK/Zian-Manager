package com.ianblk.zianmanager.client;
import com.google.gson.*;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import java.util.*;
import java.util.function.Consumer;
public final class ChoiceScreen extends ManagerThemedScreen {
 private final Screen parent;private final JsonArray source;private final boolean none;private final Consumer<String> choose;private int page,left,top,w,h;private String query="";
 public ChoiceScreen(Screen parent,String title,JsonArray source,boolean none,Consumer<String> choose){super(Component.literal(title));this.parent=parent;this.source=source;this.none=none;this.choose=choose;}
 @Override protected void init(){w=Math.min(370,width-24);h=Math.min(290,height-24);left=(width-w)/2;top=(height-h)/2;var search=new EditBox(font,left+12,top+32,w-24,20,Component.literal("Buscar"));search.setValue(query);search.setResponder(v->{query=v;page=0;rebuildWidgets();});addRenderableWidget(search);setInitialFocus(search);
  var options=new ArrayList<JsonObject>();if(none){var o=new JsonObject();o.addProperty("id","");o.addProperty("name","Sin recompensa");options.add(o);}for(var e:source)if(e.getAsJsonObject().get("name").getAsString().toLowerCase(Locale.ROOT).contains(query.toLowerCase(Locale.ROOT)))options.add(e.getAsJsonObject());int per=Math.max(1,(h-96)/24),pages=Math.max(1,(options.size()+per-1)/per);page=Math.min(page,pages-1);
  for(int i=page*per;i<Math.min(options.size(),(page+1)*per);i++){var e=options.get(i);addRenderableWidget(themed(left+12,top+62+(i-page*per)*24,w-24,20,Component.literal(e.get("name").getAsString()),b->choose.accept(e.get("id").getAsString())));}
  var back=addRenderableWidget(themed(left+12,top+h-28,30,20,Component.literal("<"),b->{page=Math.max(0,page-1);rebuildWidgets();}));back.active=page>0;var next=addRenderableWidget(themed(left+48,top+h-28,30,20,Component.literal(">"),b->{page=Math.min(pages-1,page+1);rebuildWidgets();}));next.active=page+1<pages;addRenderableWidget(themed(left+w-92,top+h-28,80,20,Component.literal("Volver"),b->onClose()));
 }
 @Override public void onClose(){minecraft.setScreen(parent);}
 @Override public void render(GuiGraphics g,int mx,int my,float dt){panel(g,left,top,w,h);super.render(g,mx,my,dt);}
}
