package com.ianblk.zianmanager.client;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.core.registries.BuiltInRegistries;
import java.util.function.Consumer;
import java.util.*;
public final class EntitySelectorScreen extends ManagerThemedScreen {
    private final Screen parent;private final Consumer<String> choose;private String filter="";private int page;
    public EntitySelectorScreen(Screen parent,Consumer<String> choose){super(Component.literal("Seleccionar entidad registrada"));this.parent=parent;this.choose=choose;}
    @Override public boolean isPauseScreen(){return false;}
    @Override protected void init(){
        var search=new EditBox(font,width/2-150,28,300,20,Component.literal("Buscar"));search.setValue(filter);search.setResponder(v->{filter=v;page=0;rebuildWidgets();});addRenderableWidget(search);setInitialFocus(search);
        var list=BuiltInRegistries.ENTITY_TYPE.keySet().stream().map(Object::toString).filter(s->s.contains(filter.toLowerCase(Locale.ROOT))).sorted().toList();int per=Math.max(1,(height-90)/24);int pages=Math.max(1,(list.size()+per-1)/per);page=Math.min(page,pages-1);
        for(int i=page*per;i<Math.min(list.size(),(page+1)*per);i++){String id=list.get(i);addRenderableWidget(themed(width/2-150,58+(i-page*per)*24,300,20,Component.literal(id),b->choose.accept(id)));}
        addRenderableWidget(themed(width/2-150,height-26,40,20,Component.literal("<"),b->{page=Math.max(0,page-1);rebuildWidgets();}));addRenderableWidget(themed(width/2-105,height-26,40,20,Component.literal(">"),b->{page=Math.min(pages-1,page+1);rebuildWidgets();}));addRenderableWidget(themed(width/2+65,height-26,85,20,Component.literal("Volver"),b->minecraft.setScreen(parent)));
    }
    @Override public void render(GuiGraphics g,int x,int y,float dt){panel(g,4,4,width-8,height-8);super.render(g,x,y,dt);}
}
