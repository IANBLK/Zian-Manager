package com.ianblk.zianmanager.client;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
/** Charcoal/gold style adapted from the owner's MIT Zian GUI theme. */
public abstract class ManagerThemedScreen extends Screen {
    protected ManagerThemedScreen(Component title){super(title);}
    @Override public boolean isPauseScreen(){return false;}
    // Screen.render calls this after our panel. Never apply vanilla blur over UI.
    @Override public void renderBackground(GuiGraphics g,int x,int y,float dt){}
    protected void panel(GuiGraphics g,int x,int y,int w,int h){
        g.fill(0,0,width,height,0x90000000);
        g.fill(x,y,x+w,y+h,0xFF181818);
        g.fill(x,y,x+w,y+3,0xFFF2C14E);
        g.drawCenteredString(font,title,width/2,y+12,0xFFF2C14E);
    }
    protected static Button themed(int x,int y,int w,int h,Component text,Button.OnPress action){return new ThemeButton(x,y,w,h,text,action);}
    private static final class ThemeButton extends Button {
        ThemeButton(int x,int y,int w,int h,Component text,OnPress action){super(x,y,w,h,text,action,DEFAULT_NARRATION);setTooltip(Tooltip.create(text));}
        @Override protected void renderWidget(GuiGraphics g,int mx,int my,float dt){
            int x=getX(),y=getY();g.fill(x,y,x+width,y+height,active?0xFFF2C14E:0xFF666666);
            g.fill(x+1,y+1,x+width-1,y+height-1,isHoveredOrFocused() && active?0xFF3A3326:0xFF242424);
            var f=Minecraft.getInstance().font;
            g.drawCenteredString(f,f.plainSubstrByWidth(getMessage().getString(),Math.max(1,width-8)),x+width/2,y+(height-f.lineHeight)/2,active?0xFFF7E2AC:0xFF999999);
        }
    }
}
