package com.ianblk.zianmanager.client;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import java.util.function.BiConsumer;
public final class TimerPositionScreen extends ManagerThemedScreen {
 private final String title;private final Screen parent;private final BiConsumer<Integer,Integer> accept;private int x,y;private boolean dragging;private double dx,dy;
 public TimerPositionScreen(Screen parent,String title,int x,int y,BiConsumer<Integer,Integer> accept){super(Component.translatableWithFallback("ui.zianmanager.194e645b09fe", "Mover temporizador"));this.title=title;this.parent=parent;this.x=x;this.y=y;this.accept=accept;}
 @Override protected void init(){fitViewport(1,1);addRenderableWidget(themed(width/2-104,height-30,100,20,Component.translatableWithFallback("ui.zianmanager.4a8396ed986a", "Guardar posición"),b->accept.accept(x,y)));addRenderableWidget(themed(width/2+4,height-30,100,20,Component.translatableWithFallback("ui.zianmanager.c111e0ab9ddb", "Cancelar"),b->onClose()));}
 @Override public boolean canvasMouseClicked(double mx,double my,int button){if(super.canvasMouseClicked(mx,my,button))return true;int left=Math.max(0,(width-DungeonTimerHud.panelWidth(title))*x/100),top=Math.max(0,(height-DungeonTimerHud.panelHeight())*y/100);if(button==0 && mx>=left && mx<=left+DungeonTimerHud.panelWidth(title) && my>=top && my<=top+DungeonTimerHud.panelHeight()){dragging=true;dx=mx-left;dy=my-top;return true;}return false;}
 @Override public boolean canvasMouseDragged(double mx,double my,int button,double vx,double vy){if(dragging && button==0){x=(int)Math.round(Math.max(0,Math.min(100,100*(mx-dx)/Math.max(1,width-DungeonTimerHud.panelWidth(title)))));y=(int)Math.round(Math.max(0,Math.min(100,100*(my-dy)/Math.max(1,height-DungeonTimerHud.panelHeight()))));return true;}return super.canvasMouseDragged(mx,my,button,vx,vy);}
 @Override public boolean canvasMouseReleased(double mx,double my,int button){dragging=false;return super.canvasMouseReleased(mx,my,button);}
 @Override public void onClose(){minecraft.setScreen(parent);}
 @Override protected void renderCanvas(GuiGraphics g,int mx,int my,float dt){g.fill(0,0,width,height,0x66101010);g.drawCenteredString(font,"Arrastra el temporizador a donde quieras",width/2,12,0xFFF2CB68);DungeonTimerHud.draw(g,x,y,"00:30:00",title);renderWidgets(g,mx,my,dt);}
}
