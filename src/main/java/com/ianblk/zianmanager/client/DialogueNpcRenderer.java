package com.ianblk.zianmanager.client;
import com.ianblk.zianmanager.ManagerNpcs.DialogueNpc;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.resources.ResourceLocation;
/** Player-model rendering adapted from ZianRCT CustomTrainerRenderer. */
public final class DialogueNpcRenderer extends MobRenderer<DialogueNpc,PlayerModel<DialogueNpc>> {
    public DialogueNpcRenderer(EntityRendererProvider.Context context){super(context,new PlayerModel<>(context.bakeLayer(ModelLayers.PLAYER_SLIM),true),0.5f);}
    @Override public ResourceLocation getTextureLocation(DialogueNpc npc){return ResourceLocation.fromNamespaceAndPath("zianmanager","textures/entity/npcs/"+npc.skin()+".png");}
}
