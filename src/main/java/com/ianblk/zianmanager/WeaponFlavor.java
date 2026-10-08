package com.ianblk.zianmanager;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.ChatFormatting;
import java.util.List;
public final class WeaponFlavor {
 private WeaponFlavor(){}
 public static void append(ItemStack stack,List<Component> lines,int seconds){String id=net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath();lines.add(Component.translatable("flavor.zianmanager."+id+".kind").withStyle(ChatFormatting.DARK_AQUA));lines.add(Component.translatable("flavor.zianmanager."+id+".line1").withStyle(ChatFormatting.GRAY,ChatFormatting.ITALIC));lines.add(Component.translatable("flavor.zianmanager."+id+".line2").withStyle(ChatFormatting.GRAY,ChatFormatting.ITALIC));if(seconds>0){lines.add(Component.empty());lines.add(Component.translatable("tooltip.zianmanager.cooldown",seconds).withStyle(ChatFormatting.GOLD));}}
}
