package com.ianblk.zianmanager.client;
import com.google.gson.*;
import com.ianblk.zianmanager.ManagerNetwork;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.client.gui.components.Tooltip;
import net.neoforged.neoforge.network.PacketDistributor;
public final class PersonalRewardsScreen extends ManagerThemedScreen {
 private final net.minecraft.client.gui.screens.Screen parent;
 public net.minecraft.client.gui.screens.Screen parent(){return parent;}
 private final JsonObject data;private int page,left,top;private boolean pending;
 public PersonalRewardsScreen(JsonObject data,net.minecraft.client.gui.screens.Screen parent){super(Component.translatableWithFallback("zianmanager.rewards.title","Mis recompensas"));this.data=data;this.parent=parent;}
 @Override protected void init(){fitViewport(520,360);left=(width-460)/2;top=(height-320)/2;var entries=data.getAsJsonArray("entries");page=Math.min(page,Math.max(0,(entries.size()-1)/7));
  for(int n=page*7;n<Math.min(entries.size(),(page+1)*7);n++){var row=entries.get(n).getAsJsonObject();boolean review=row.get("review").getAsBoolean();String label=row.get("reference").getAsString()+" · "+(review?Component.translatableWithFallback("zianmanager.rewards.in_review","En revisión").getString():Component.translatableWithFallback("zianmanager.rewards.claim","Reclamar").getString());var button=addRenderableWidget(themed(left+12,top+57+(n%7)*27,436,23,Component.literal(label),b->{if(pending)return;pending=true;var request=new JsonObject();request.addProperty("id",row.get("id").getAsString());PacketDistributor.sendToServer(new ManagerNetwork.Action(java.util.UUID.fromString(data.get("token").getAsString()),"claim",request.toString()));rebuildWidgets();}));button.active=!pending&&!review;button.setTooltip(Tooltip.create(Component.literal(row.get("origin").getAsString()+" · "+(review?Component.translatableWithFallback("zianmanager.rewards.review_tip","Un administrador debe verificar la entrega").getString():Component.translatableWithFallback("zianmanager.rewards.claim_tip","Solo se reclamará tu propio premio").getString()))));}
  var previous=addRenderableWidget(themed(left+12,top+254,100,20,Component.translatableWithFallback("zianmanager.common.previous","Anterior"),b->{page--;rebuildWidgets();}));previous.active=page>0&&!pending;
  var next=addRenderableWidget(themed(left+348,top+254,100,20,Component.translatableWithFallback("zianmanager.common.next","Siguiente"),b->{page++;rebuildWidgets();}));next.active=(page+1)*7<entries.size()&&!pending;
  addRenderableWidget(themed(left+12,top+286,130,20,Component.translatableWithFallback("zianmanager.rewards.check_status","Consultar estado"),b->PacketDistributor.sendToServer(new ManagerNetwork.Request("my_rewards",""))));
  addRenderableWidget(themed(left+318,top+286,130,20,Component.translatableWithFallback("zianmanager.common.close","Cerrar"),b->onClose()));
 }
 @Override protected void renderCanvas(GuiGraphics g,int mx,int my,float partial){panel(g,left,top,460,320);String notice=Ui.notice(data.get("notice").getAsString());Component text=pending?Component.translatableWithFallback("zianmanager.rewards.waiting","Esperando respuesta. Consultar estado no repite la entrega."):!notice.isBlank()?Component.literal(notice):data.getAsJsonArray("entries").isEmpty()?Component.translatableWithFallback("zianmanager.rewards.empty","No tienes recompensas pendientes."):Component.translatableWithFallback("zianmanager.rewards.separate","Pendientes y entregas en revisión se muestran por separado.");int lineY=top+34;for(var line:font.split(text,436)){g.drawString(font,line,left+12,lineY,0xFFBBBBBB);lineY+=10;}renderWidgets(g,mx,my,partial);}
 @Override public void onClose(){if(parent!=null)minecraft.setScreen(parent);else super.onClose();}
}
