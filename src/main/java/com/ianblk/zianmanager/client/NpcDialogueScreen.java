package com.ianblk.zianmanager.client;
import com.google.gson.*;
import com.ianblk.zianmanager.ManagerNetwork;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.network.PacketDistributor;
import java.util.UUID;
public final class NpcDialogueScreen extends ManagerThemedScreen {
 private final JsonObject data;private int left,top,w,h,scroll,textBottom;
 public NpcDialogueScreen(JsonObject data){super(Component.literal(data.get("title").getAsString()));this.data=data;}
 @Override protected void init(){w=Math.min(390,width-24);h=Math.min(310,height-24);left=(width-w)/2;top=(height-h)/2;var actions=data.getAsJsonArray("actions");int rows=(actions.size()+1)/2,start=top+h-32-rows*24;textBottom=start-10;for(int i=0;i<actions.size();i++){var action=actions.get(i).getAsJsonObject();int col=(w-30)/2;addRenderableWidget(themed(left+12+(i%2)*(col+6),start+(i/2)*24,col,20,Component.literal(action.get("label").getAsString()),b->{b.active=false;var chosen=new JsonObject();chosen.addProperty("button",action.get("id").getAsString());PacketDistributor.sendToServer(new ManagerNetwork.Action(UUID.fromString(data.get("token").getAsString()),"npc_command",chosen.toString()));onClose();}));}addRenderableWidget(themed(left+w-90,top+h-28,78,20,Component.literal("Cerrar"),b->onClose()));}
 @Override public boolean mouseScrolled(double x,double y,double dx,double dy){var lines=font.split(Component.literal(data.get("text").getAsString()),w-24);scroll=Math.max(0,Math.min(Math.max(0,lines.size()-Math.max(1,(textBottom-top-38)/font.lineHeight)),scroll-(int)dy));return true;}
 @Override public void render(GuiGraphics g,int mx,int my,float dt){panel(g,left,top,w,h);var lines=font.split(Component.literal(data.get("text").getAsString()),w-24);g.enableScissor(left+12,top+38,left+w-12,textBottom);for(int i=scroll;i<lines.size();i++)g.drawString(font,lines.get(i),left+12,top+38+(i-scroll)*font.lineHeight,0xFFDDDDDD);g.disableScissor();super.render(g,mx,my,dt);}
}
