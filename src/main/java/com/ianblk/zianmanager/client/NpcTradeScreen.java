package com.ianblk.zianmanager.client;
import com.ianblk.zianmanager.NpcTradeMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

/** Native slot coordinates fit even the smallest 320 x 240 logical viewport. */
public final class NpcTradeScreen extends AbstractContainerScreen<NpcTradeMenu> {
 private static final int PAGE_SIZE=4;
 private int page,waiting=-1;private Button exchange;
 public NpcTradeScreen(NpcTradeMenu menu,Inventory inventory,Component title){super(menu,inventory,title);imageWidth=320;imageHeight=238;}
 @Override protected void init(){super.init();buttons();}
 private void send(int id){if(minecraft.gameMode!=null)minecraft.gameMode.handleInventoryButtonClick(menu.containerId,id);}
 private void buttons(){clearWidgets();int count=menu.labels().size();page=Math.min(page,Math.max(0,(count-1)/PAGE_SIZE));
  for(int i=page*PAGE_SIZE;i<Math.min(count,page*PAGE_SIZE+PAGE_SIZE);i++){final int index=i;addRenderableWidget(new OfferButton(leftPos+8,topPos+38+(i%PAGE_SIZE)*39,index,menu.labels().get(i)));}
  addRenderableWidget(ManagerThemedScreen.themed(leftPos+8,topPos+199,26,18,Component.literal("<"),b->{page--;buttons();})).active=page>0;
  addRenderableWidget(ManagerThemedScreen.themed(leftPos+114,topPos+199,26,18,Component.literal(">"),b->{page++;buttons();})).active=(page+1)*PAGE_SIZE<count;
  exchange=addRenderableWidget(ManagerThemedScreen.themed(leftPos+150,topPos+104,162,20,Component.literal("Intercambiar"),b->send(64)));exchange.active=count>0&&(waiting<0||waiting==menu.selected());
  exchange.setTooltip(Tooltip.create(Component.literal("Entrega los materiales y recibe todos los resultados.\nIntercambios ilimitados; se cobra desde tu inventario.")));
  addRenderableWidget(ManagerThemedScreen.themed(leftPos+240,topPos+220,72,16,Component.literal("Cerrar"),b->onClose()));
 }
 @Override protected void containerTick(){super.containerTick();if(exchange!=null)exchange.active=!menu.labels().isEmpty()&&(waiting<0||waiting==menu.selected());}
 @Override protected void renderBg(GuiGraphics g,float partial,int mx,int my){g.fill(leftPos,topPos,leftPos+imageWidth,topPos+imageHeight,0xFF181818);g.fill(leftPos,topPos,leftPos+imageWidth,topPos+3,0xFFF2C14E);
  g.fill(leftPos+144,topPos+27,leftPos+145,topPos+217,0xFF433C2A);
  for(var slot:menu.slots){int x=leftPos+slot.x,y=topPos+slot.y;g.fill(x-1,y-1,x+17,y+17,slot.index<8?0xFFF2C14E:0xFF77623A);g.fill(x,y,x+16,y+16,0xFF292929);}
  g.drawString(font,"Ofertas · "+menu.labels().size(),leftPos+8,topPos+25,0xFFF2C14E,false);
  g.drawString(font,"Entregas",leftPos+150,topPos+28,0xFFDDDDDD,false);g.drawString(font,"Recibes",leftPos+150,topPos+65,0xFFDDDDDD,false);
  g.drawCenteredString(font,(page+1)+"/"+Math.max(1,(menu.labels().size()+PAGE_SIZE-1)/PAGE_SIZE),leftPos+74,topPos+204,0xFFDDDDDD);
 }
 @Override protected void renderLabels(GuiGraphics g,int x,int y){g.drawCenteredString(font,fit(title.getString(),304),160,10,0xFFF2C14E);g.drawString(font,playerInventoryTitle,150,130,0xFFBBBBBB,false);String message=switch(menu.status()){case 1->"Intercambio completado";case 2->"Te faltan materiales";case 3->"No caben las recompensas";case 4->"Oferta no disponible";case 5->"Inventario cambió; reintenta";default->"Intercambios sin límite";};g.drawString(font,fit(message,224),8,226,menu.status()==1?0xFF97D99D:menu.status()>1?0xFFFFAA88:0xFFBBBBBB,false);}
 private String fit(String text,int width){return font.width(text)<=width?text:font.plainSubstrByWidth(text,width-font.width("…"))+"…";}
 private final class OfferButton extends Button {
  private final int index;private final String pay,result;
  OfferButton(int x,int y,int index,String text){super(x,y,132,35,Component.literal((index+1)+". "+text),b->{waiting=index;exchange.active=index==menu.selected();send(index);},DEFAULT_NARRATION);this.index=index;int arrow=text.indexOf('→');pay=(index+1)+". "+(arrow<0?text:text.substring(0,arrow).trim());result=arrow<0?"Ver objetos a la derecha":"→ "+text.substring(arrow+1).trim();setTooltip(Tooltip.create(Component.literal(text+"\nIntercambios ilimitados")));}
  @Override protected void renderWidget(GuiGraphics g,int mx,int my,float dt){boolean selected=index==(waiting>=0?waiting:menu.selected());int x=getX(),y=getY();g.fill(x,y,x+width,y+height,selected?0xFFF2C14E:0xFF77623A);g.fill(x+1,y+1,x+width-1,y+height-1,isHoveredOrFocused()?0xFF3A3326:selected?0xFF332D20:0xFF242424);g.drawString(font,fit(pay,width-12),x+6,y+7,0xFFF7E2AC,false);g.drawString(font,fit(result,width-12),x+6,y+20,0xFFCCCCCC,false);}
 }
 @Override public void render(GuiGraphics g,int mx,int my,float partial){renderBackground(g,mx,my,partial);super.render(g,mx,my,partial);renderTooltip(g,mx,my);}
}
