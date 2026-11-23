package com.ianblk.zianmanager;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.ChatFormatting;
import java.util.List;
public final class WeaponFlavor {
 private WeaponFlavor(){}
 private static int color(String id){if(id.startsWith("reimagined_")){if(id.contains("diamond"))return 0x48E4DA;if(id.contains("iron"))return 0xCBD5E1;if(id.contains("wooden"))return 0x88D868;if(id.contains("stone"))return 0xB8ABA0;return 0xF2CB68;}if(id.contains("common"))return 0xCBD5E1;if(id.contains("rare"))return 0x79B8FF;if(id.contains("epic"))return 0xC979FF;if(id.contains("cosmetic"))return 0x48E4DA;if(id.contains("blue_"))return 0x79B8FF;if(id.contains("green_"))return 0x88D868;if(id.contains("grey_"))return 0xC8D1DA;if(id.contains("wither") || id.contains("necromancer") || id.contains("void"))return 0xC979FF;if(id.contains("ninja") || id.contains("tide") || id.contains("ancient"))return 0x48E4DA;if(id.contains("flame") || id.contains("chainsaw") || id.contains("red_"))return 0xFF8569;return 0xF2CB68;}
 public static Component name(ItemStack stack){String id=net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath();return Component.translatable(stack.getItem().getDescriptionId()).withStyle(s->s.withColor(color(id)).withBold(true));}
 public static void append(ItemStack stack,List<Component> lines,int seconds){append(stack,lines,seconds,0);}
 private static boolean shift(){return net.neoforged.fml.loading.FMLEnvironment.dist.isClient() && com.ianblk.zianmanager.client.TooltipKeys.shift();}
 public static void append(ItemStack stack,List<Component> lines,int cooldown,int duration){String id=net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath();int c=color(id);for(com.ianblk.zianmanager.core.TooltipLayout.Line l:com.ianblk.zianmanager.core.TooltipLayout.lines(new com.ianblk.zianmanager.core.TooltipLayout.Spec(id,duration,cooldown),shift())){switch(l.kind()){case KIND,LEGEND->lines.add(Component.translatable(l.key()).withStyle(s->s.withColor(c).withBold(true)));case LORE->lines.add(Component.translatable(l.key()).withStyle(ChatFormatting.GRAY,ChatFormatting.ITALIC));case DETAIL->lines.add(Component.translatable(l.key()).withStyle(ChatFormatting.DARK_GRAY));case BLANK->lines.add(Component.empty());case ABILITY->lines.add(Component.translatable(l.key()).withStyle(ChatFormatting.GOLD));case DURATION,COOLDOWN->lines.add(Component.translatable(l.key(),l.arg()).withStyle(ChatFormatting.GOLD));case RARITY->lines.add(Component.translatable(l.key()).withStyle(s->s.withColor(c)));case HINT->lines.add(Component.translatable(l.key()).withStyle(ChatFormatting.DARK_GRAY,ChatFormatting.ITALIC));}}}
}
