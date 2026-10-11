package com.ianblk.zianmanager.client;
import com.ianblk.zianmanager.ManagerNetwork;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
public final class DungeonTimerHud {
 private static ManagerNetwork.Timer state;private static long received;
 public static void accept(ManagerNetwork.Timer value){state=value;received=System.nanoTime();}
 public static void clear(){state=null;}
 public static void render(RenderGuiEvent.Post event){var game=Minecraft.getInstance();if(game.screen instanceof TimerPositionScreen || state==null || !state.visible() || game.player==null || game.level==null || game.options.hideGui || System.nanoTime()-received>3500000000L)return;long left=Math.max(0,state.remaining()-(System.nanoTime()-received)/1000000000L);String time=state.unlimited()?"Tiempo ilimitado":String.format(java.util.Locale.ROOT,"%02d:%02d:%02d",left/3600,left/60%60,left%60);draw(event.getGuiGraphics(),state.x(),state.y(),time,state.title());}
 public static int panelWidth(String title){var game=Minecraft.getInstance();return Math.min(Math.min(game.getWindow().getGuiScaledWidth()-8,180),Math.max(game.font.width("✦ "+title),game.font.width("Tiempo ilimitado"))+16);}
 public static void draw(GuiGraphics g,int xPercent,int yPercent,String time,String title){var game=Minecraft.getInstance();int w=panelWidth(title),h=34;int x=Math.max(0,(game.getWindow().getGuiScaledWidth()-w)*xPercent/100),y=Math.max(0,(game.getWindow().getGuiScaledHeight()-h)*yPercent/100);g.fill(x,y,x+w,y+h,0xCC141414);g.fill(x,y,x+w,y+2,0xFFE7BE57);g.drawString(game.font,game.font.plainSubstrByWidth("✦ "+title,Math.max(0,w-16)),x+8,y+6,0xFFF2CB68);g.drawString(game.font,time,x+8,y+20,0xFFFFFFFF);}
}
