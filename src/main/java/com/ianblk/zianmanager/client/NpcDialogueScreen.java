package com.ianblk.zianmanager.client;
import com.google.gson.JsonObject;
import com.ianblk.zianmanager.ManagerNetwork;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.network.PacketDistributor;
import java.util.UUID;
public final class NpcDialogueScreen extends ManagerThemedScreen {
    private final JsonObject data;private int left,top,w,h,scroll;
    public NpcDialogueScreen(JsonObject data){super(Component.literal(data.get("title").getAsString()));this.data=data;}
    @Override protected void init(){w=Math.min(360,width-24);h=Math.min(240,height-24);left=(width-w)/2;top=(height-h)/2;
        if(data.get("available").getAsBoolean())addRenderableWidget(themed(left+12,top+h-32,w-114,20,Component.literal(data.get("button").getAsString()),b->{b.active=false;PacketDistributor.sendToServer(new ManagerNetwork.Action(UUID.fromString(data.get("token").getAsString()),"npc_command","{}"));onClose();}));
        addRenderableWidget(themed(left+w-90,top+h-32,78,20,Component.literal("Cerrar"),b->onClose()));
    }
    @Override public boolean mouseScrolled(double x,double y,double dx,double dy){var lines=font.split(Component.literal(data.get("text").getAsString()),w-24);scroll=Math.max(0,Math.min(Math.max(0,lines.size()-(h-80)/font.lineHeight),scroll-(int)dy));return true;}
    @Override public void render(GuiGraphics g,int mx,int my,float dt){panel(g,left,top,w,h);var lines=font.split(Component.literal(data.get("text").getAsString()),w-24);g.enableScissor(left+12,top+38,left+w-12,top+h-44);for(int i=scroll;i<lines.size();i++)g.drawString(font,lines.get(i),left+12,top+38+(i-scroll)*font.lineHeight,0xFFDDDDDD);g.disableScissor();super.render(g,mx,my,dt);}
}
