package com.ianblk.zianmanager.client;
import net.minecraft.client.Minecraft;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import com.google.gson.JsonParser;
public final class ManagerClient {
    private static String pending;
    public static void accept(String json){pending=json;}
    public static void init(net.neoforged.bus.api.IEventBus bus){bus.addListener((net.neoforged.neoforge.client.event.EntityRenderersEvent.RegisterRenderers e)->e.registerEntityRenderer(com.ianblk.zianmanager.ManagerNpcs.NPC.get(),DialogueNpcRenderer::new));NeoForge.EVENT_BUS.addListener((ClientTickEvent.Post e)->{var game=Minecraft.getInstance();if(pending!=null && game.player!=null){String data=pending;pending=null;var json=JsonParser.parseString(data).getAsJsonObject();game.setScreen(json.get("type").getAsString().equals("dialogue")?new NpcDialogueScreen(json):new ManagerScreen(json));}});}
}
