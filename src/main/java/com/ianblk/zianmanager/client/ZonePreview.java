package com.ianblk.zianmanager.client;
import com.google.gson.*;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.*;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
public final class ZonePreview {
 private static final net.minecraft.client.renderer.MultiBufferSource.BufferSource OUTLINE_BUFFERS=net.minecraft.client.renderer.MultiBufferSource.immediate(new com.mojang.blaze3d.vertex.ByteBufferBuilder(8192));
 private static JsonObject zone;private static long expires;public static boolean visible=true;
 public static void accept(JsonObject data){if(data.has("zonePreview")){zone=data.getAsJsonObject("zonePreview").deepCopy();expires=System.currentTimeMillis()+600000;visible=true;}}
 public static void clear(){zone=null;}
 private static boolean active(){var game=Minecraft.getInstance();return visible && zone!=null && game.level!=null && game.player!=null && System.currentTimeMillis()<expires && game.level.dimension().location().toString().equals(zone.get("dimension").getAsString());}
 public static void render(RenderLevelStageEvent event){if(event.getStage()!=RenderLevelStageEvent.Stage.AFTER_PARTICLES || !active())return;var game=Minecraft.getInstance();var camera=event.getCamera();var pos=camera.getPosition();var pose=new PoseStack();pose.pushPose();pose.translate(-pos.x,-pos.y,-pos.z);var buffers=OUTLINE_BUFFERS;var lines=buffers.getBuffer(RenderType.lines());JsonObject first=point("first"),second=point("second");if(first!=null && second!=null){var box=new AABB(Math.min(x(first),x(second)),Math.min(y(first),y(second)),Math.min(z(first),z(second)),Math.max(x(first),x(second))+1,Math.max(y(first),y(second))+2,Math.max(z(first),z(second))+1);LevelRenderer.renderLineBox(pose,lines,box,0.3f,0.75f,1,1);}if(point("center")!=null)mark(point("center"),"Centro de la dungeon",pose,buffers,1,0.85f,0.15f);if(point("center")==null && first!=null)mark(first,"Punto 1",pose,buffers,0.2f,1,0.3f);if(point("center")==null && second!=null)mark(second,"Punto 2",pose,buffers,1,0.25f,0.2f);if(zone.has("points")){int i=0;for(var p:zone.getAsJsonArray("points"))mark(p.getAsJsonObject(),"Mob "+(++i),pose,buffers,1,0.8f,0.2f);}buffers.endBatch();pose.popPose();}
 private static void mark(JsonObject p,String label,PoseStack pose,net.minecraft.client.renderer.MultiBufferSource buffers,float r,float g,float b){double x=x(p),y=y(p),z=z(p);LevelRenderer.renderLineBox(pose,buffers.getBuffer(RenderType.lines()),new AABB(Math.floor(x),Math.floor(y),Math.floor(z),Math.floor(x)+1,Math.floor(y)+1,Math.floor(z)+1),r,g,b,1);pose.pushPose();pose.translate(Math.floor(x)+0.5,Math.floor(y)+1.4,Math.floor(z)+0.5);pose.mulPose(Minecraft.getInstance().getEntityRenderDispatcher().cameraOrientation());pose.scale(0.025f,-0.025f,0.025f);var text=Component.literal(label+" · "+(int)Math.floor(x)+", "+(int)Math.floor(y)+", "+(int)Math.floor(z));var font=Minecraft.getInstance().font;font.drawInBatch(text,-font.width(text)/2f,0,0xFFFFFFFF,false,pose.last().pose(),buffers,Font.DisplayMode.SEE_THROUGH,0x66000000,LightTexture.FULL_BRIGHT);pose.popPose();}
 public static void hud(RenderGuiEvent.Post e){if(!active())return;var game=Minecraft.getInstance();var g=e.getGuiGraphics();int y=game.getWindow().getGuiScaledHeight()-66;String text=(zone.has("preview")?"Vista previa sin guardar: ":"Zona: ")+zone.get("id").getAsString()+(point("center")==null?" · Punto 1 verde / Punto 2 rojo / Mobs dorados":" · Centro dorado / Contorno azul / Apariciones doradas");text=game.font.plainSubstrByWidth(text,game.getWindow().getGuiScaledWidth()-16);g.fill(6,y-3,game.font.width(text)+12,y+12,0x99000000);g.drawString(game.font,text,9,y,0xFFF7E2AC);}
 private static JsonObject point(String key){return zone.has(key) && !zone.get(key).isJsonNull()?zone.getAsJsonObject(key):null;}
 private static double x(JsonObject p){return p.get("x").getAsDouble();}private static double y(JsonObject p){return p.get("y").getAsDouble();}private static double z(JsonObject p){return p.get("z").getAsDouble();}
}
