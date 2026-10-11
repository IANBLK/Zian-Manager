package com.ianblk.zianmanager.client;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.*;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.layers.CustomHeadLayer;
import net.minecraft.world.entity.*;
import net.minecraft.world.item.*;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
/** Armor layer renders the original baked item model through the blocks atlas. */
public final class ImportedHelmetModel extends Model {
 private final ModelPart head;private final ItemStack stack;private final LivingEntity entity;
 private ImportedHelmetModel(HumanoidModel<?> original,ItemStack stack,LivingEntity entity){super(RenderType::entityCutoutNoCull);this.head=original.head;this.stack=stack;this.entity=entity;}
 public static IClientItemExtensions extension(){return new IClientItemExtensions(){@Override public Model getGenericArmorModel(LivingEntity entity,ItemStack stack,EquipmentSlot slot,HumanoidModel<?> original){return new ImportedHelmetModel(original,stack,entity);}};}
 @Override public void renderToBuffer(PoseStack pose,VertexConsumer consumer,int light,int overlay,int color){pose.pushPose();head.translateAndRotate(pose);CustomHeadLayer.translateToHead(pose,entity instanceof net.minecraft.world.entity.npc.Villager);var renderer=Minecraft.getInstance().getItemRenderer();var model=renderer.getModel(stack,entity.level(),entity,0);model=net.neoforged.neoforge.client.ClientHooks.handleCameraTransforms(pose,model,ItemDisplayContext.HEAD,false);pose.translate(-0.5,-0.5,-0.5);renderer.renderModelLists(model,stack,light,overlay,pose,consumer);pose.popPose();}
}
