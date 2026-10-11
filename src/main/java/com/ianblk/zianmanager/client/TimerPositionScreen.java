package com.ianblk.zianmanager.client;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import java.util.function.BiConsumer;
public final class TimerPositionScreen extends ManagerThemedScreen {
 private final String title;private final Screen parent;private final BiConsumer<Integer,Integer> accept;private int x,y;private boolean dragging;private double dx,dy;
 public TimerPositionScreen(Screen parent,String title,int x,int y,BiConsumer<Integer,Integer> accept){super(Component.literal("Mover temporizador"));this.title=title;this.parent=parent;this.x=x;this.y=y;this.accept=accept;}
 @Override protected void init(){addRenderableWidget(themed(width/2-104,height-30,100,20,Component.literal("Guardar posición"),b->accept.accept(x,y)));addRenderableWidget(themed(width/2+4,height-30,100,20,Component.literal("Cancelar"),b->onClose()));}
 @Override public boolean mouseClicked(double mx,double my,int button){if(super.mouseClicked(mx,my,button))return true;int left=Math.max(0,(width-DungeonTimerHud.panelWidth(title))*x/100),top=Math.max(0,(height-34)*y/100);if(button==0 && mx>=left && mx<=left+DungeonTimerHud.panelWidth(title) && my>=top && my<=top+34){dragging=true;dx=mx-left;dy=my-top;return true;}return false;}
 @Override public boolean mouseDragged(double mx,double my,int button,double vx,double vy){if(dragging && button==0){x=(int)Math.round(Math.max(0,Math.min(100,100*(mx-dx)/Math.max(1,width-DungeonTimerHud.panelWidth(title)))));y=(int)Math.round(Math.max(0,Math.min(100,100*(my-dy)/Math.max(1,height-34))));return true;}return super.mouseDragged(mx,my,button,vx,vy);}
 @Override public boolean mouseReleased(double mx,double my,int button){dragging=false;return super.mouseReleased(mx,my,button);}
 @Override public void onClose(){minecraft.setScreen(parent);}
 @Override public void render(GuiGraphics g,int mx,int my,float dt){g.fill(0,0,width,height,0x66101010);g.drawCenteredString(font,"Arrastra el temporizador a donde quieras",width/2,12,0xFFF2CB68);DungeonTimerHud.draw(g,x,y,"00:30:00",title);super.render(g,mx,my,dt);}
}
