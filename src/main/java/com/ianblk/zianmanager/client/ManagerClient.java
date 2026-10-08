package com.ianblk.zianmanager.client;
import net.minecraft.client.Minecraft;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import com.google.gson.JsonParser;
public final class ManagerClient {
    private static String pending;
    public static void accept(String json){pending=json;}
    public static void init(){NeoForge.EVENT_BUS.addListener((ClientTickEvent.Post e)->{var game=Minecraft.getInstance();if(pending!=null && game.player!=null){String data=pending;pending=null;game.setScreen(new ManagerScreen(JsonParser.parseString(data).getAsJsonObject()));}});}
}
