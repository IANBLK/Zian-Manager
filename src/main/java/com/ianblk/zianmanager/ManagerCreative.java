package com.ianblk.zianmanager;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
public final class ManagerCreative {
 private static final DeferredRegister<CreativeModeTab> TABS=DeferredRegister.create(Registries.CREATIVE_MODE_TAB,"zianmanager");
 static{TABS.register("equipment",()->CreativeModeTab.builder().title(Component.literal("Zian Manager")).icon(()->new ItemStack(ManagerEquipment.item("flame_spear"))).displayItems((parameters,out)->{ManagerBlocks.CRATES.values().forEach(b->out.accept(b.get().asItem()));ManagerEquipment.ALL.values().forEach(i->out.accept(i.get()));}).build());}
 public static void register(IEventBus bus){TABS.register(bus);}
}
