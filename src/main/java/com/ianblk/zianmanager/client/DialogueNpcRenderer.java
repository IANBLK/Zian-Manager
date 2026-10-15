package com.ianblk.zianmanager.client;
import com.ianblk.zianmanager.ManagerNpcs.DialogueNpc;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.resources.ResourceLocation;
import com.ianblk.zianmanager.core.NpcSkins;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
/** Player-model rendering adapted from ZianRCT CustomTrainerRenderer. */
public final class DialogueNpcRenderer extends MobRenderer<DialogueNpc,PlayerModel<DialogueNpc>> {
    private final PlayerModel<DialogueNpc> slimModel;
    private final PlayerModel<DialogueNpc> classicModel;
    public DialogueNpcRenderer(EntityRendererProvider.Context context){
        super(context,new PlayerModel<>(context.bakeLayer(ModelLayers.PLAYER_SLIM),true),0.5f);
        slimModel=model;classicModel=new PlayerModel<>(context.bakeLayer(ModelLayers.PLAYER),false);
    }
    @Override public void render(DialogueNpc npc,float yaw,float partialTick,PoseStack pose,MultiBufferSource buffers,int light){
        model=NpcSkins.get(npc.skin()).slim()?slimModel:classicModel;
        super.render(npc,yaw,partialTick,pose,buffers,light);
    }
    @Override public ResourceLocation getTextureLocation(DialogueNpc npc){return ResourceLocation.fromNamespaceAndPath("zianmanager","textures/entity/npcs/"+NpcSkins.get(npc.skin()).id()+".png");}
}
