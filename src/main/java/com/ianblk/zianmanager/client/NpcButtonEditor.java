package com.ianblk.zianmanager.client;
import com.google.gson.JsonObject;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import java.util.*;
import java.util.function.Consumer;
public final class NpcButtonEditor extends ManagerThemedScreen {
 private final Screen parent;private final Consumer<JsonObject> accept;private final String id;private String label,command,seconds,notice="";private int left,top,w,h;
 public NpcButtonEditor(Screen parent,JsonObject before,Consumer<JsonObject> accept){super(Component.literal("Configurar botón de comando"));this.parent=parent;this.accept=accept;id=before==null?"btn_"+UUID.randomUUID().toString().substring(0,8):before.get("id").getAsString();label=before==null?"Continuar":before.get("label").getAsString();command=before==null?"":before.get("command").getAsString();seconds=before==null?"60":before.get("cooldownSeconds").getAsString();}
 @Override protected void init(){w=Math.min(390,width-24);h=Math.min(234,height-24);left=(width-w)/2;top=(height-h)/2;box(top+50,64,label,v->label=v,"Texto");box(top+92,2048,command,v->command=v,"Comando");box(top+134,10,seconds,v->seconds=v,"Espera");addRenderableWidget(themed(left+12,top+h-28,(w-30)/2,20,Component.literal("Aplicar"),b->{try{var validated=new com.ianblk.zianmanager.core.Definitions.NpcButton(id,label,command,Integer.parseInt(seconds));var out=new JsonObject();out.addProperty("id",validated.id());out.addProperty("label",validated.label());out.addProperty("command",validated.command());out.addProperty("cooldownSeconds",validated.cooldownSeconds());accept.accept(out);}catch(Exception error){notice="Revisa texto, comando y espera (0–2592000).";}}));addRenderableWidget(themed(left+18+(w-30)/2,top+h-28,(w-30)/2,20,Component.literal("Cancelar"),b->onClose()));}
 private void box(int y,int length,String value,Consumer<String> change,String title){var box=new EditBox(font,left+12,y,w-24,20,Component.literal(title));box.setMaxLength(length);box.setValue(value);box.setResponder(change);addRenderableWidget(box);}
 @Override public void onClose(){minecraft.setScreen(parent);}
 @Override public void render(GuiGraphics g,int mx,int my,float dt){panel(g,left,top,w,h);g.drawString(font,"Texto del botón",left+12,top+38,0xFFDDDDDD);g.drawString(font,"Comando · {player} = jugador",left+12,top+80,0xFFDDDDDD);g.drawString(font,"Espera por jugador (segundos)",left+12,top+122,0xFFDDDDDD);if(!notice.isEmpty())g.drawString(font,font.plainSubstrByWidth(notice,w-24),left+12,top+163,0xFFF7E2AC);super.render(g,mx,my,dt);}
}
