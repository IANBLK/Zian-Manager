package com.ianblk.zianmanager.client;
import com.ianblk.zianmanager.ManagerNetwork;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
public final class DungeonTimerHud {
 private static ManagerNetwork.Timer state;private static long received;
 public static void accept(ManagerNetwork.Timer value){state=value;received=System.nanoTime();}
 public static void clear(){state=null;}
 public static void render(RenderGuiEvent.Post event){var game=Minecraft.getInstance();if(game.screen instanceof TimerPositionScreen || state==null || !state.visible() || game.player==null || game.level==null || game.options.hideGui || System.nanoTime()-received>3500000000L)return;long left=Math.max(0,state.remaining()-(System.nanoTime()-received)/1000000000L);String time=state.unlimited()?"Tiempo Ilimitado":String.format(java.util.Locale.ROOT,"%02d:%02d:%02d",left/3600,left/60%60,left%60);draw(event.getGuiGraphics(),state.x(),state.y(),time,state.title());}
 private static double scale(){var w=Minecraft.getInstance().getWindow();return Math.min(1.0,Math.min(w.getGuiScaledWidth()/600.0,w.getGuiScaledHeight()/360.0));}
 private static int contentWidth(String title){var game=Minecraft.getInstance();return Math.max(1,Math.min((int)((game.getWindow().getGuiScaledWidth()-8)/scale()),Math.min(180,Math.max(game.font.width("✦ "+title),game.font.width("Tiempo Ilimitado"))+16)));}
 public static int panelWidth(String title){return (int)Math.ceil(contentWidth(title)*scale());}
 public static int panelHeight(){return (int)Math.ceil(34*scale());}
 public static void draw(GuiGraphics g,int xPercent,int yPercent,String time,String title){var game=Minecraft.getInstance();int w=contentWidth(title),h=34;int x=Math.max(0,(game.getWindow().getGuiScaledWidth()-panelWidth(title))*xPercent/100),y=Math.max(0,(game.getWindow().getGuiScaledHeight()-panelHeight())*yPercent/100);g.pose().pushPose();try{g.pose().translate(x,y,0);g.pose().scale((float)scale(),(float)scale(),1);g.fill(0,0,w,h,0xCC141414);g.fill(0,0,w,2,0xFFE7BE57);g.drawString(game.font,game.font.plainSubstrByWidth("✦ "+title,Math.max(0,w-16)),8,6,0xFFF2CB68);g.drawString(game.font,game.font.plainSubstrByWidth(time,Math.max(0,w-16)),8,20,0xFFFFFFFF);}finally{g.pose().popPose();}}
}
