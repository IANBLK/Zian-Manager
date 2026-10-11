package com.ianblk.zianmanager.client;
import com.ianblk.zianmanager.ManagerEquipment;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.*;
public final class TideProjectileRenderer extends EntityRenderer<ManagerEquipment.TideProjectile> {
 private final net.minecraft.client.renderer.entity.ItemRenderer items;
 public TideProjectileRenderer(EntityRendererProvider.Context context){super(context);items=context.getItemRenderer();}
 @Override public void render(ManagerEquipment.TideProjectile entity,float yaw,float partial,PoseStack pose,MultiBufferSource buffers,int light){pose.pushPose();pose.mulPose(Axis.YP.rotationDegrees(Mth.lerp(partial,entity.yRotO,entity.getYRot())-90));pose.mulPose(Axis.ZP.rotationDegrees(Mth.lerp(partial,entity.xRotO,entity.getXRot())-90));var stack=new ItemStack(ManagerEquipment.item("altar_tide"));if(entity.isFoil())stack.set(net.minecraft.core.component.DataComponents.ENCHANTMENT_GLINT_OVERRIDE,true);items.renderStatic(stack,ItemDisplayContext.FIXED,light,net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY,pose,buffers,entity.level(),entity.getId());pose.popPose();super.render(entity,yaw,partial,pose,buffers,light);}
 @Override public ResourceLocation getTextureLocation(ManagerEquipment.TideProjectile entity){return ResourceLocation.fromNamespaceAndPath("zianmanager","textures/item/imported/altar_tide/0.png");}
}
