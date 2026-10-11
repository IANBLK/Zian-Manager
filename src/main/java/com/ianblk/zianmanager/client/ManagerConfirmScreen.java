package com.ianblk.zianmanager.client;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import java.util.function.Consumer;
public final class ManagerConfirmScreen extends ManagerThemedScreen {
    private final Consumer<Boolean> answer;private final Component message;private int left,top,w,h;
    public ManagerConfirmScreen(Consumer<Boolean> answer,Component title,Component message){super(title);this.answer=answer;this.message=message;}
    @Override protected void init(){w=Math.min(340,width-24);h=Math.min(150,height-24);left=(width-w)/2;top=(height-h)/2;
        addRenderableWidget(themed(left+12,top+h-32,(w-32)/2,20,Component.literal("Confirmar"),b->answer.accept(true)));
        addRenderableWidget(themed(left+20+(w-32)/2,top+h-32,(w-32)/2,20,Component.literal("Cancelar"),b->answer.accept(false)));
    }
    @Override public void onClose(){answer.accept(false);}
    @Override public void render(GuiGraphics g,int mx,int my,float dt){panel(g,left,top,w,h);g.drawWordWrap(font,message,left+12,top+42,w-24,0xFFAAAAAA);super.render(g,mx,my,dt);}
}
