package com.ianblk.zianmanager;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.core.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.registries.*;
import java.util.*;
/** Original item models with vanilla tool behavior and real armor semantics. */
public final class ImportedGear {
 public static final Map<String,Item> VANILLA=new LinkedHashMap<>();
 public static final List<String> TOOLS=new ArrayList<>();
 private record DiamondPlusTier(Tier base) implements Tier {public int getUses(){return base.getUses()+75;}public float getSpeed(){return base.getSpeed();}public float getAttackDamageBonus(){return base.getAttackDamageBonus();}public TagKey<Block> getIncorrectBlocksForDrops(){return base.getIncorrectBlocksForDrops();}public int getEnchantmentValue(){return base.getEnchantmentValue();}public Ingredient getRepairIngredient(){return base.getRepairIngredient();}}
 public static void register(DeferredRegister.Items items,Map<String,DeferredItem<? extends Item>> all){
  for(String material:List.of("wooden","stone","iron","gold","diamond")){Tier tier=new DiamondPlusTier(Tiers.DIAMOND);
   for(String type:List.of("axe","pickaxe","shovel","sword")){String id="reimagined_"+material+"_"+type;TOOLS.add(id);String vanillaId="diamond_"+type;VANILLA.put(id,net.minecraft.core.registries.BuiltInRegistries.ITEM.get(ResourceLocation.withDefaultNamespace(vanillaId)));
    all.put(id,items.register(id,()->{float axeDamage=5,axeSpeed=-3f;var p=new Item.Properties();return switch(type){case "axe"->new NamedAxe(tier,p.attributes(AxeItem.createAttributes(tier,axeDamage,axeSpeed)));case "pickaxe"->new NamedPickaxe(tier,p.attributes(DiggerItem.createAttributes(tier,1,-2.8f)));case "shovel"->new NamedShovel(tier,p.attributes(DiggerItem.createAttributes(tier,1.5f,-3f)));default->new NamedSword(tier,p.attributes(SwordItem.createAttributes(tier,3,-2.4f)));};}));
   }
  }
 }
 private static final class NamedAxe extends AxeItem {NamedAxe(Tier t,Properties p){super(t,p);}@Override public Component getName(ItemStack s){return WeaponFlavor.name(s);}}
 private static final class NamedPickaxe extends PickaxeItem {NamedPickaxe(Tier t,Properties p){super(t,p);}@Override public Component getName(ItemStack s){return WeaponFlavor.name(s);}}
 private static final class NamedShovel extends ShovelItem {NamedShovel(Tier t,Properties p){super(t,p);}@Override public Component getName(ItemStack s){return WeaponFlavor.name(s);}}
 private static final class NamedSword extends SwordItem {NamedSword(Tier t,Properties p){super(t,p);}@Override public Component getName(ItemStack s){return WeaponFlavor.name(s);}}
}
