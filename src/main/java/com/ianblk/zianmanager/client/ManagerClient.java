package com.ianblk.zianmanager.client;
import net.minecraft.client.Minecraft;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import com.google.gson.JsonParser;
public final class ManagerClient {
    private static String pending;
    public static void accept(String json){pending=json;}
    public static void init(net.neoforged.bus.api.IEventBus bus){NeoForge.EVENT_BUS.addListener(ZonePreview::render);NeoForge.EVENT_BUS.addListener(ZonePreview::hud);bus.addListener((net.neoforged.neoforge.client.event.EntityRenderersEvent.RegisterRenderers e)->{e.registerEntityRenderer(com.ianblk.zianmanager.ManagerNpcs.NPC.get(),DialogueNpcRenderer::new);e.registerEntityRenderer(com.ianblk.zianmanager.ManagerEquipment.BOLT.get(),context->new net.minecraft.client.renderer.entity.ThrownItemRenderer<>(context,0.75f,true));e.registerEntityRenderer(com.ianblk.zianmanager.ManagerEquipment.TIDE_PROJECTILE.get(),TideProjectileRenderer::new);e.registerBlockEntityRenderer(com.ianblk.zianmanager.ManagerBlocks.CHEST_ENTITY.get(),CrateRenderer::new);});bus.addListener((net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent e)->e.registerReloadListener((net.minecraft.server.packs.resources.ResourceManagerReloadListener) manager->CrateRenderer.clear()));NeoForge.EVENT_BUS.addListener((ClientTickEvent.Post e)->{var game=Minecraft.getInstance();if(game.level==null)ZonePreview.clear();if(pending!=null && game.player!=null){String data=pending;pending=null;var json=JsonParser.parseString(data).getAsJsonObject();ZonePreview.accept(json);game.setScreen(json.get("type").getAsString().equals("dialogue")?new NpcDialogueScreen(json):json.get("type").getAsString().equals("loot")?new LootEditorScreen(json):new ManagerScreen(json));}});}
}
