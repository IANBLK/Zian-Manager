package com.ianblk.zianmanager.client;
import com.google.gson.*;
import com.ianblk.zianmanager.ManagerNetwork;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.network.PacketDistributor;
import java.util.UUID;

public final class NpcTradesEditor extends ManagerThemedScreen {
    private final JsonObject data;private int page,selected=-1,left,top,w,h;private boolean confirm;
    public NpcTradesEditor(JsonObject data){super(Component.literal(data.get("title").getAsString()));this.data=data;}
    private void send(String op){var json=new JsonObject();json.addProperty("index",selected);PacketDistributor.sendToServer(new ManagerNetwork.Action(UUID.fromString(data.get("token").getAsString()),op,json.toString()));clearWidgets();}
    @Override protected void init(){fitViewport(680,450);w=Math.min(610,width-24);h=Math.min(400,height-24);left=(width-w)/2;top=(height-h)/2;var rows=data.getAsJsonArray("offers");
        for(int i=page*4;i<Math.min(rows.size(),page*4+4);i++){int index=i;addRenderableWidget(themed(left+12,top+87+(i%4)*24,w-24,21,Component.literal((selected==i?"✓ ":"")+(i+1)+". "+rows.get(i).getAsString()),b->{selected=index;confirm=false;rebuildWidgets();}));}
        addRenderableWidget(themed(left+12,top+h-178,30,20,Component.literal("<"),b->{page--;rebuildWidgets();})).active=page>0;addRenderableWidget(themed(left+48,top+h-178,30,20,Component.literal(">"),b->{page++;rebuildWidgets();})).active=(page+1)*4<rows.size();
        addRenderableWidget(themed(left+84,top+h-178,w-96,20,Component.literal(confirm?"Confirmar quitar oferta "+(selected+1):"Quitar seleccionada"),b->{if(confirm)send("trade_remove");else{confirm=true;rebuildWidgets();}})).active=selected>=0;
        addRenderableWidget(themed(left+12,top+h-60,(w-30)/2,22,Component.literal("Preparar desde inventario"),b->send("trade_prepare")));
        addRenderableWidget(themed(left+18+(w-30)/2,top+h-60,(w-30)/2,22,Component.literal("Guardar oferta preparada"),b->send("trade_add"))).active=!data.get("draft").getAsString().isBlank()&&rows.size()<64;
        addRenderableWidget(themed(left+w-90,top+h-30,78,20,Component.literal("Cerrar"),b->onClose()));
    }
    @Override protected void renderCanvas(GuiGraphics g,int mx,int my,float partial){panel(g,left,top,w,h);g.drawString(font,"Ranuras 1–4: pagos · 5: separador · 6–9: recompensas",left+12,top+35,0xFFDDDDDD);g.drawString(font,"La cantidad es la de cada pila; las ranuras vacías se ignoran.",left+12,top+48,0xFFDDDDDD);g.drawString(font,"Configurar no consume objetos. Existencias ilimitadas.",left+12,top+61,0xFFF2C14E);g.drawString(font,font.plainSubstrByWidth(data.get("notice").getAsString(),w-24),left+12,top+74,0xFFAAAAAA);
        g.drawString(font,"Oferta preparada: pagos → recompensas",left+12,top+h-145,0xFFF2C14E);int bx=left+(w-9*28)/2,by=top+h-125;
        for(int i=0;i<9;i++){int x=bx+i*28;g.fill(x-1,by-1,x+19,by+19,i==4?0xFFF2C14E:0xFF77623A);g.fill(x,by,x+18,by+18,0xFF292929);g.drawCenteredString(font,i==4?"│":""+(i+1),x+9,by+22,0xFFBBBBBB);if(i==4){g.drawCenteredString(font,"→",x+9,by+5,0xFFF2C14E);continue;}int at=i<4?i:i-1;if(data.has("draftSlots")&&at<data.getAsJsonArray("draftSlots").size()){var icon=data.getAsJsonArray("draftSlots").get(at).getAsJsonObject();if(icon.has("id")){var item=net.minecraft.core.registries.BuiltInRegistries.ITEM.get(net.minecraft.resources.ResourceLocation.parse(icon.get("id").getAsString()));var stack=new net.minecraft.world.item.ItemStack(item,icon.get("count").getAsInt());g.renderItem(stack,x+1,by+1);g.renderItemDecorations(font,stack,x+1,by+1);if(mx>=x&&mx<x+18&&my>=by&&my<by+18)g.renderTooltip(font,Component.literal(icon.get("name").getAsString()),mx,my);}}}
        renderWidgets(g,mx,my,partial);
    }
}
