package com.ianblk.zianmanager.client;
import com.google.gson.JsonArray;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import java.util.function.Consumer;
public final class EntriesScreen extends ManagerThemedScreen {
    private final Screen parent;private final JsonArray entries;private final Consumer<String> choose;private int page;
    public EntriesScreen(Screen parent,JsonArray entries,Consumer<String> choose){super(Component.literal("Entradas · porcentaje de primera selección"));this.parent=parent;this.entries=entries;this.choose=choose;}
    @Override public boolean isPauseScreen(){return false;}
    @Override protected void init(){int per=Math.max(1,(height-60)/24);int pages=Math.max(1,(entries.size()+per-1)/per);page=Math.min(page,pages-1);for(int i=page*per;i<Math.min(entries.size(),(page+1)*per);i++){var entry=entries.get(i).getAsJsonObject();addRenderableWidget(themed(10,30+(i-page*per)*24,width-20,20,Component.literal(entry.get("name").getAsString()),b->choose.accept(entry.get("id").getAsString())));}
        addRenderableWidget(themed(10,height-24,40,20,Component.literal("<"),b->{page=Math.max(0,page-1);rebuildWidgets();}));addRenderableWidget(themed(55,height-24,40,20,Component.literal(">"),b->{page=Math.min(pages-1,page+1);rebuildWidgets();}));addRenderableWidget(themed(width-90,height-24,80,20,Component.literal("Volver"),b->minecraft.setScreen(parent)));}
    @Override public void render(GuiGraphics g,int x,int y,float dt){panel(g,4,4,width-8,height-8);super.render(g,x,y,dt);}
}
