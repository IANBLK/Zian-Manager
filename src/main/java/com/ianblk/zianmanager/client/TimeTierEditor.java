package com.ianblk.zianmanager.client;
import com.google.gson.JsonObject;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import java.util.function.Consumer;
public final class TimeTierEditor extends ManagerThemedScreen {
 private final Screen parent;private final Consumer<JsonObject> accept;private String name,permission,weekday,weekend,notice="";private int left,top,w,h;
 public TimeTierEditor(Screen parent,JsonObject before,Consumer<JsonObject> accept){super(Component.literal("Configurar rango"));this.parent=parent;this.accept=accept;name=before==null?"ELITE":before.get("name").getAsString();permission=before==null?"zianmanager.dungeon.elite":before.get("permission").getAsString();weekday=before==null?"240":before.get("weekday").getAsString();weekend=before==null?"360":before.get("weekend").getAsString();}
 @Override protected void init(){w=Math.min(390,width-24);h=Math.min(278,height-16);left=(width-w)/2;top=(height-h)/2;box(top+50,64,name,v->name=v,"Nombre");box(top+92,128,permission,v->permission=v,"Permiso");box(top+134,8,weekday,v->weekday=v,"Entre semana");box(top+176,8,weekend,v->weekend=v,"Fin de semana");addRenderableWidget(themed(left+12,top+h-28,(w-30)/2,20,Component.literal("Aplicar"),b->{try{var v=new com.ianblk.zianmanager.core.DailyDungeonTime.Tier(name,permission,Integer.parseInt(weekday),Integer.parseInt(weekend));var out=new JsonObject();out.addProperty("name",v.name());out.addProperty("permission",v.permission());out.addProperty("weekday",v.weekday());out.addProperty("weekend",v.weekend());accept.accept(out);}catch(Exception error){notice="Revisa nombre, permiso y minutos (1–1440).";}}));addRenderableWidget(themed(left+18+(w-30)/2,top+h-28,(w-30)/2,20,Component.literal("Cancelar"),b->onClose()));}
 private void box(int y,int length,String value,Consumer<String> change,String title){var box=new EditBox(font,left+12,y,w-24,20,Component.literal(title));box.setMaxLength(length);box.setValue(value);box.setResponder(change);addRenderableWidget(box);}
 @Override public void onClose(){minecraft.setScreen(parent);}
 @Override public void render(GuiGraphics g,int mx,int my,float dt){panel(g,left,top,w,h);g.drawString(font,"Nombre del rango",left+12,top+38,0xFFDDDDDD);g.drawString(font,"Permiso de LuckPerms",left+12,top+80,0xFFDDDDDD);g.drawString(font,"Lunes a viernes (minutos)",left+12,top+122,0xFFDDDDDD);g.drawString(font,"Sábado y domingo (minutos)",left+12,top+164,0xFFDDDDDD);if(!notice.isEmpty())g.drawString(font,font.plainSubstrByWidth(notice,w-24),left+12,top+h-52,0xFFF7E2AC);super.render(g,mx,my,dt);}
}
