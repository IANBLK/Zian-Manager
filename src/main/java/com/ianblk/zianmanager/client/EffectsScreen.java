package com.ianblk.zianmanager.client;
import com.google.gson.*;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import java.util.*;
import java.util.function.Consumer;
public final class EffectsScreen extends ManagerThemedScreen {
 private final Screen parent;private final Consumer<String> accept;private final List<String> effects=new ArrayList<>();private String selected="minecraft:speed",level="1",seconds="600",notice="";private int left,top,w,h,page;
 public EffectsScreen(Screen parent,String before,Consumer<String> accept){super(Component.literal("Efectos del mob"));this.parent=parent;this.accept=accept;if(!before.isBlank())for(String raw:before.split(";")){try{var bits=raw.split(",");if(bits.length!=3)throw new IllegalArgumentException();var key=net.minecraft.resources.ResourceLocation.tryParse(bits[0]);if(key==null || !BuiltInRegistries.MOB_EFFECT.containsKey(key))throw new IllegalArgumentException();new com.ianblk.zianmanager.core.Definitions.Effect(bits[0],Integer.parseInt(bits[1]),Integer.parseInt(bits[2]));effects.add(raw);}catch(Exception error){notice="Hay efectos con formato incorrecto en Avanzado.";}}}
 @Override protected void init(){w=Math.min(390,width-24);h=Math.min(280,height-24);left=(width-w)/2;top=(height-h)/2;
  addRenderableWidget(themed(left+12,top+36,w-24,20,Component.literal("Efecto: "+Component.translatable(BuiltInRegistries.MOB_EFFECT.get(net.minecraft.resources.ResourceLocation.parse(selected)).getDescriptionId()).getString()),b->{var options=new JsonArray();for(var id:BuiltInRegistries.MOB_EFFECT.keySet()){var e=new JsonObject();e.addProperty("id",id.toString());e.addProperty("name",Component.translatable(BuiltInRegistries.MOB_EFFECT.get(id).getDescriptionId()).getString());options.add(e);}minecraft.setScreen(new ChoiceScreen(this,"Elegir efecto",options,false,v->{selected=v;minecraft.setScreen(this);}));}));
  var l=new EditBox(font,left+12,top+76,(w-30)/2,20,Component.literal("Nivel"));l.setValue(level);l.setResponder(v->level=v);addRenderableWidget(l);var s=new EditBox(font,left+18+(w-30)/2,top+76,(w-30)/2,20,Component.literal("Duración"));s.setValue(seconds);s.setResponder(v->seconds=v);addRenderableWidget(s);
  addRenderableWidget(themed(left+12,top+102,w-24,20,Component.literal("Añadir efecto"),b->{try{int n=Integer.parseInt(level),t=Integer.parseInt(seconds);if(n<1 || n>11 || t<1 || t>86400 || effects.size()>=8)throw new IllegalArgumentException();effects.add(selected+","+(n-1)+","+t);notice="";rebuildWidgets();}catch(Exception e){notice="Nivel: 1–11; duración: 1–86400 seg; máximo 8.";}}));
  int per=Math.max(1,(h-188)/24),pages=Math.max(1,(effects.size()+per-1)/per);page=Math.min(page,pages-1);for(int i=page*per;i<Math.min(effects.size(),(page+1)*per);i++){int at=i;var bits=effects.get(i).split(",");String name=Component.translatable(BuiltInRegistries.MOB_EFFECT.get(net.minecraft.resources.ResourceLocation.parse(bits[0])).getDescriptionId()).getString();addRenderableWidget(themed(left+12,top+130+(i-page*per)*24,w-24,20,Component.literal(name+" · nivel "+(Integer.parseInt(bits[1])+1)+" · "+bits[2]+" seg · Quitar"),b->{effects.remove(at);rebuildWidgets();}));}
  addRenderableWidget(themed(left+12,top+h-54,30,20,Component.literal("<"),b->{page=Math.max(0,page-1);rebuildWidgets();}));addRenderableWidget(themed(left+48,top+h-54,30,20,Component.literal(">"),b->{page=Math.min(pages-1,page+1);rebuildWidgets();}));addRenderableWidget(themed(left+12,top+h-28,(w-30)/2,20,Component.literal("Aplicar efectos"),b->accept.accept(String.join(";",effects))));addRenderableWidget(themed(left+18+(w-30)/2,top+h-28,(w-30)/2,20,Component.literal("Cancelar"),b->onClose()));
 }
 @Override public void onClose(){minecraft.setScreen(parent);}
 @Override public void render(GuiGraphics g,int mx,int my,float dt){panel(g,left,top,w,h);g.drawString(font,"Nivel del efecto",left+12,top+64,0xFFDDDDDD);g.drawString(font,"Duración en segundos",left+18+(w-30)/2,top+64,0xFFDDDDDD);if(!notice.isEmpty())g.drawString(font,font.plainSubstrByWidth(notice,w-102),left+90,top+h-48,0xFFF7E2AC);super.render(g,mx,my,dt);}
}
