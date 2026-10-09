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
 public static final List<String> HATS=List.of("hat_aviator","hat_axolotl_hood","hat_bloom_crown","hat_bumblebee","hat_bunny_beanie","hat_chef","hat_corsair","hat_cozy_pom","hat_dusty_cowboy","hat_fiesta","hat_fox_ears","hat_frog_bucket","hat_frost_diadem","hat_jellyfish","hat_little_dragon","hat_midnight_wizard","hat_miner","hat_panda_cap","hat_parcel","hat_retro_tv","hat_royal_crown","hat_rubber_duck","hat_sea_captain","hat_steam_topper","hat_straw_garden","hat_street_cap","hat_sushi","hat_toadstool","hat_trailblazer","hat_ufo");
 public static final Map<String,Item> VANILLA=new LinkedHashMap<>();
 public static final List<String> TOOLS=new ArrayList<>();
 private record ShortTier(Tier base) implements Tier {public int getUses(){return 75;}public float getSpeed(){return base.getSpeed();}public float getAttackDamageBonus(){return base.getAttackDamageBonus()+1;}public TagKey<Block> getIncorrectBlocksForDrops(){return base.getIncorrectBlocksForDrops();}public int getEnchantmentValue(){return base.getEnchantmentValue();}public Ingredient getRepairIngredient(){return base.getRepairIngredient();}}
 public static void register(DeferredRegister.Items items,Map<String,DeferredItem<? extends Item>> all){
  for(String id:HATS)all.put(id,items.register(id,()->new Helmet(new Item.Properties().durability(ArmorItem.Type.HELMET.getDurability(37)).fireResistant().attributes(helmetAttributes()))));
  for(String material:List.of("wooden","stone","iron","gold","diamond")){Tier vanilla=switch(material){case "wooden"->Tiers.WOOD;case "stone"->Tiers.STONE;case "iron"->Tiers.IRON;case "gold"->Tiers.GOLD;default->Tiers.DIAMOND;};Tier tier=new ShortTier(vanilla);
   for(String type:List.of("axe","pickaxe","shovel","sword")){String id="reimagined_"+material+"_"+type;TOOLS.add(id);String vanillaId=(material.equals("gold")?"golden":material)+"_"+type;VANILLA.put(id,net.minecraft.core.registries.BuiltInRegistries.ITEM.get(ResourceLocation.withDefaultNamespace(vanillaId)));
    all.put(id,items.register(id,()->{float axeDamage=material.equals("stone")?7:material.equals("diamond")?5:6,axeSpeed=material.equals("wooden") || material.equals("stone")?-3.2f:material.equals("iron")?-3.1f:-3f;var p=new Item.Properties();return switch(type){case "axe"->new NamedAxe(tier,p.attributes(AxeItem.createAttributes(tier,axeDamage,axeSpeed)));case "pickaxe"->new NamedPickaxe(tier,p.attributes(DiggerItem.createAttributes(tier,1,-2.8f)));case "shovel"->new NamedShovel(tier,p.attributes(DiggerItem.createAttributes(tier,1.5f,-3f)));default->new NamedSword(tier,p.attributes(SwordItem.createAttributes(tier,3,-2.4f)));};}));
   }
  }
 }
 private static ItemAttributeModifiers helmetAttributes(){var id=ResourceLocation.withDefaultNamespace("armor.helmet");return ItemAttributeModifiers.builder().add(Attributes.ARMOR,new AttributeModifier(id,4,AttributeModifier.Operation.ADD_VALUE),EquipmentSlotGroup.HEAD).add(Attributes.ARMOR_TOUGHNESS,new AttributeModifier(id,3,AttributeModifier.Operation.ADD_VALUE),EquipmentSlotGroup.HEAD).add(Attributes.KNOCKBACK_RESISTANCE,new AttributeModifier(id,0.1,AttributeModifier.Operation.ADD_VALUE),EquipmentSlotGroup.HEAD).build();}
 public static final class Helmet extends ArmorItem {
  public Helmet(Properties p){super(ArmorMaterials.NETHERITE,Type.HELMET,p);}
  @Override public int getDefense(){return 4;}
  @Override public ItemAttributeModifiers getDefaultAttributeModifiers(){return helmetAttributes();}
  @Override public Component getName(ItemStack stack){return WeaponFlavor.name(stack);}
  @Override public ResourceLocation getArmorTexture(ItemStack stack,Entity entity,EquipmentSlot slot,ArmorMaterial.Layer layer,boolean inner){return ResourceLocation.withDefaultNamespace("textures/atlas/blocks.png");}
 }
 private static final class NamedAxe extends AxeItem {NamedAxe(Tier t,Properties p){super(t,p);}@Override public Component getName(ItemStack s){return WeaponFlavor.name(s);}}
 private static final class NamedPickaxe extends PickaxeItem {NamedPickaxe(Tier t,Properties p){super(t,p);}@Override public Component getName(ItemStack s){return WeaponFlavor.name(s);}}
 private static final class NamedShovel extends ShovelItem {NamedShovel(Tier t,Properties p){super(t,p);}@Override public Component getName(ItemStack s){return WeaponFlavor.name(s);}}
 private static final class NamedSword extends SwordItem {NamedSword(Tier t,Properties p){super(t,p);}@Override public Component getName(ItemStack s){return WeaponFlavor.name(s);}}
}
