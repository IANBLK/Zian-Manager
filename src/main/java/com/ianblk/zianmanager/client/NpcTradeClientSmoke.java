package com.ianblk.zianmanager.client;
import com.google.gson.*;
import com.ianblk.zianmanager.*;
import com.ianblk.zianmanager.core.NpcSkins;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.*;
import net.minecraft.world.item.trading.*;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import java.nio.file.Files;
import java.util.*;

/** Isolated, opt-in GUI and baked player-model visual evidence. */
public final class NpcTradeClientSmoke {
 private static int stage=-1,ticks;
 public static void register(){if("true".equals(System.getenv("ZIAN_NPC_TRADE_CLIENT_SMOKE")))net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener(NpcTradeClientSmoke::tick);}
 private static void tick(ClientTickEvent.Post event){var mc=Minecraft.getInstance();if(mc.getOverlay()!=null||mc.screen==null)return;try{
  if(!mc.gameDirectory.toPath().toAbsolutePath().toString().contains("npc-trade-client-smoke"))throw new IllegalStateException("Isolated client profile required");
  mc.options.pauseOnLostFocus=false;if(stage<0){stage=0;open(mc);return;}if(++ticks<25)return;ticks=0;
  if(stage<6&&(int)mc.getWindow().getGuiScale()!=2+stage/2)throw new IllegalStateException("GUI scale was clamped");
  Screenshot.grab(mc.gameDirectory,"npc-"+stage+".png",mc.getMainRenderTarget(),c->ZianManager.LOGGER.info("NPC client capture {}",c.getString()));
  if(++stage<9)open(mc);else{Files.writeString(mc.gameDirectory.toPath().resolve("npc-client-pass.txt"),"PASS: trade/editor GUI at native scales 2,3,4; 27 supplied skins with front/back baked classic/slim player models.\n");mc.stop();}
 }catch(Throwable error){ZianManager.LOGGER.error("NPC client visual smoke failed",error);try{Files.writeString(mc.gameDirectory.toPath().resolve("npc-client-failed.txt"),error.toString());}catch(Exception ignored){}mc.stop();}}
 private static void open(Minecraft mc){if(stage>=6){mc.options.guiScale().set(2);mc.resizeDisplay();mc.setScreen(new SkinCatalog(stage-6));return;}mc.options.guiScale().set(2+stage/2);mc.resizeDisplay();
  if(stage%2==0){var d=new JsonObject();d.addProperty("type","npc_trades");d.addProperty("token",UUID.randomUUID().toString());d.addProperty("title","Comercio · Herrero del reino");d.addProperty("notice","Solo administradores configuran las ofertas");d.addProperty("draft","3 pagos → 2 recompensas");var rows=new JsonArray();for(int i=0;i<8;i++)rows.add("4 × Hierro bruto + 1 × Diamante + 1 × Llave → 2 × Oro + 1 × Martillo");d.add("offers",rows);var icons=new JsonArray();for(var item:List.of(Items.RAW_IRON,Items.DIAMOND,ManagerEquipment.item("common_key"),Items.AIR,Items.GOLD_INGOT,ManagerEquipment.item("blue_hammer"),Items.AIR,Items.AIR)){var icon=new JsonObject();if(item!=Items.AIR){icon.addProperty("id",net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(item).toString());icon.addProperty("name",item.getDescription().getString());icon.addProperty("count",item==Items.RAW_IRON?4:1);}icons.add(icon);}d.add("draftSlots",icons);mc.setScreen(new NpcTradesEditor(d));}
  else{var inventory=new Inventory(null);var row=new NpcTrading.Stacks(List.of(new ItemStack(Items.RAW_IRON,4),new ItemStack(Items.DIAMOND),new ItemStack(ManagerEquipment.item("common_key"))),List.of(new ItemStack(Items.GOLD_INGOT,2),new ItemStack(ManagerEquipment.item("blue_hammer"))));var rows=Collections.nCopies(8,row);var labels=Collections.nCopies(8,"4 Hierro + 2 materiales → Oro + Martillo");var menu=new NpcTradeMenu(1,inventory,null,0,rows,labels);mc.setScreen(new net.minecraft.client.gui.screens.Screen(Component.literal("Vista del comerciante")){private final NpcTradeScreen child=new NpcTradeScreen(menu,inventory,Component.literal("Herrero del reino"));@Override protected void init(){child.init(minecraft,width,height);}@Override public void render(GuiGraphics g,int mx,int my,float partial){child.render(g,mx,my,partial);}});}

 }
 private static final class SkinCatalog extends ManagerThemedScreen {
  private final int page;private net.minecraft.client.model.PlayerModel<net.minecraft.world.entity.LivingEntity> slim,classic;
  SkinCatalog(int page){super(Component.literal("Skins · modelos nativos · "+(page+1)+"/3"));this.page=page;}
  @Override protected void init(){fitViewport(920,610);var models=minecraft.getEntityModels();classic=new net.minecraft.client.model.PlayerModel<>(models.bakeLayer(net.minecraft.client.model.geom.ModelLayers.PLAYER),false);slim=new net.minecraft.client.model.PlayerModel<>(models.bakeLayer(net.minecraft.client.model.geom.ModelLayers.PLAYER_SLIM),true);}
  @Override protected void renderCanvas(GuiGraphics g,int mx,int my,float partial){int x=(width-900)/2,y=(height-590)/2;panel(g,x,y,900,590);var skins=NpcSkins.ALL.stream().filter(s->s.id().equals("heraldo_real")||s.id().matches("alaric_zafiro|soren_hielo|nyx_umbral|iris_prismatica|kael_invierno|elian_alba|darian_roble|varek_carmesi|liora_arena|vigia_sculk|morgana_cosecha|heraldo_vacio|sombra_violeta|centinela_carmesi|nadir_medianoche|guardia_cobalto|serafina_nieves|lucien_ocaso|mika_rosada|espectro_sculk|ignis_brasero|noir_eclipse|silas_obsidiana|amelia_flores|sol_ambar|magnus_cobre")).toList();
   for(int i=page*9;i<Math.min(skins.size(),(page+1)*9);i++){var skin=skins.get(i);int bx=x+12+((i-page*9)%3)*295,by=y+42+((i-page*9)/3)*178;g.drawString(font,skin.name(),bx,by,0xFFF2C14E);g.drawString(font,skin.slim()?"Brazos delgados":"Brazos gruesos",bx+125,by+25,0xFFBBBBBB);avatar(g,skin,bx+46,by+64,false);avatar(g,skin,bx+102,by+64,true);}
  }
  private void avatar(GuiGraphics g,NpcSkins.Skin skin,int x,int y,boolean back){var pose=g.pose();pose.pushPose();pose.translate(x,y,200);pose.scale(52,52,52);pose.mulPose(com.mojang.math.Axis.YP.rotationDegrees(back?0:180));var buffers=minecraft.renderBuffers().bufferSource();var model=skin.slim()?slim:classic;com.mojang.blaze3d.platform.Lighting.setupForEntityInInventory();model.renderToBuffer(pose,buffers.getBuffer(net.minecraft.client.renderer.RenderType.entityCutoutNoCull(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("zianmanager","textures/entity/npcs/"+skin.id()+".png"))),15728880,net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY);buffers.endBatch();pose.popPose();com.mojang.blaze3d.platform.Lighting.setupFor3DItems();}
 }
}
