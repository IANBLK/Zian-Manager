package com.ianblk.zianmanager;
import com.ianblk.zianmanager.core.MiningPlane;
import net.minecraft.core.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.*;
import net.minecraft.world.*;
import net.minecraft.world.effect.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.SmallFireball;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.*;
import net.minecraft.tags.BlockTags;
import net.minecraft.sounds.*;
import net.minecraft.core.particles.ParticleTypes;
import net.neoforged.bus.api.*;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.registries.*;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.entity.player.*;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import java.util.*;
public final class ManagerEquipment {
 private static final DeferredRegister.Items ITEMS=DeferredRegister.createItems("zianmanager");
 private static final DeferredRegister<EntityType<?>> ENTITIES=DeferredRegister.create(Registries.ENTITY_TYPE,"zianmanager");
 public static final Set<String> RETIRED=Set.of("altar_void3","altar_void2","altar_void1","altar_soul","altar_hotbarsym","altar_handle","altar_warden","altar_harness","altar_dragonheart","altar_clockdragonrend","altar_arrowdragonrend","straw_hat","shield1");
 public static boolean retired(Item item){return RETIRED.contains(net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(item).getPath()) && net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(item).getNamespace().equals("zianmanager");}
 public static final Map<String,DeferredItem<? extends Item>> ALL=new LinkedHashMap<>();
 public static final DeferredHolder<EntityType<?>,EntityType<FlameBolt>> BOLT=ENTITIES.register("flame_bolt",()->EntityType.Builder.<FlameBolt>of(FlameBolt::new,MobCategory.MISC).sized(0.3125f,0.3125f).clientTrackingRange(8).updateInterval(1).build("zianmanager:flame_bolt"));
 public enum Power{NONE(0),FLAME(1),SONIC(10),GLADIATOR(90),REGEN(90),NIGHT(90),TIDE(90),DRAGON(90),WITHER(90),WARRIOR(90),NECROMANCER(90),NINJA(90),CHAINSAW(90);public final int seconds;Power(int seconds){this.seconds=seconds;}}
 private record Hit(BlockPos pos,Direction face,long at){}
 private record Job(ServerPlayer player,BlockPos pos,BlockState before,Direction face,Item item){}
 private static final Map<UUID,Hit> HITS=new HashMap<>();private static final Map<UUID,Job> JOBS=new HashMap<>();private static final Set<UUID> MINING=new HashSet<>();
 private static final String COOLDOWNS="ZianManagerPowers";
 static{
  for(String color:List.of("blue","green","grey","red"))ALL.put(color+"_hammer",ITEMS.register(color+"_hammer",()->new Hammer(new Item.Properties().rarity(Rarity.UNCOMMON).attributes(DiggerItem.createAttributes(Tiers.DIAMOND,1,-2.8f)))));
  sword("flame_spear",Tiers.DIAMOND,Power.FLAME);sword("void_staff",Tiers.NETHERITE,Power.SONIC);sword("gladiator_sword",Tiers.NETHERITE,Power.GLADIATOR);
  sword("altar_ancientblade",Tiers.DIAMOND,Power.REGEN);sword("altar_dragonrend",Tiers.DIAMOND,Power.DRAGON);sword("altar_withersym",Tiers.DIAMOND,Power.WITHER);sword("altar_omen",Tiers.DIAMOND,Power.NIGHT);ALL.put("altar_tide",ITEMS.register("altar_tide",()->new TideTrident(new Item.Properties().rarity(Rarity.EPIC).durability(250).attributes(TridentItem.createAttributes()))));
  ALL.put("altar_amaxe",ITEMS.register("altar_amaxe",()->new FlavorAxe(new Item.Properties().rarity(Rarity.UNCOMMON).attributes(AxeItem.createAttributes(Tiers.DIAMOND,5,-3.0f)))));
  ALL.put("altar_ampick",ITEMS.register("altar_ampick",()->new FlavorPickaxe(new Item.Properties().rarity(Rarity.UNCOMMON).attributes(DiggerItem.createAttributes(Tiers.DIAMOND,1,-2.8f)))));
  newSword("warrior_reskin",11,Power.WARRIOR,-2.4f);newSword("necromancer_reskin",9,Power.NECROMANCER,-2.4f);newSword("ninja_reskin",10,Power.NINJA,-2.1f);newSword("chainsaw",11,Power.CHAINSAW,-2.8f);
  ImportedGear.register(ITEMS,ALL);
  for(String id:List.of("common_key","rare_key","epic_key","legendary_key"))ALL.put(id,ITEMS.register(id,()->new FlavorKey(new Item.Properties().rarity(id.equals("common_key")?Rarity.COMMON:id.equals("rare_key")?Rarity.RARE:Rarity.EPIC))));
 }
 static{for(String id:RETIRED)ITEMS.registerSimpleItem(id,new Item.Properties());}
 private static void sword(String id,Tier tier,Power power){ALL.put(id,ITEMS.register(id,()->new AbilitySword(tier,new Item.Properties().rarity(Rarity.EPIC).attributes(SwordItem.createAttributes(tier,3+(Set.of("gladiator_sword","altar_ancientblade","altar_dragonrend","altar_withersym").contains(id)?2:id.equals("altar_omen")?3:0),-2.4f)),power)));}
 private static void newSword(String id,int damage,Power power,float speed){ALL.put(id,ITEMS.register(id,()->new AbilitySword(Tiers.NETHERITE,new Item.Properties().rarity(Rarity.EPIC).attributes(SwordItem.createAttributes(Tiers.NETHERITE,damage-5,speed)),power)));}
 public static Item item(String id){return ALL.get(id).get();}
 public static void register(IEventBus bus){ITEMS.register(bus);ENTITIES.register(bus);
  NeoForge.EVENT_BUS.addListener((net.neoforged.neoforge.event.entity.EntityJoinLevelEvent e)->{if(e.getLevel() instanceof ServerLevel level && e.getEntity() instanceof net.minecraft.world.entity.projectile.ThrownTrident old && old.getType()==EntityType.TRIDENT && old.getPickupItemStackOrigin().is(item("altar_tide"))){var replacement=TIDE_PROJECTILE.get().create(level);if(replacement==null)return;replacement.load(old.saveWithoutId(new CompoundTag()));replacement.setUUID(old.getUUID());e.setCanceled(true);if(!level.addFreshEntity(replacement))ZianManager.LOGGER.error("Tide projectile spawn rejected by another mod");}});
  NeoForge.EVENT_BUS.addListener((PlayerInteractEvent.LeftClickBlock e)->{if(e.getEntity() instanceof ServerPlayer p && e.getFace()!=null)HITS.put(p.getUUID(),new Hit(e.getPos(),e.getFace(),p.serverLevel().getGameTime()));});
  NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST,(BlockEvent.BreakEvent e)->{if(!(e.getPlayer() instanceof ServerPlayer p) || !(p.getMainHandItem().getItem() instanceof Hammer) || MINING.contains(p.getUUID()) || p.isShiftKeyDown() || !e.getState().is(BlockTags.MINEABLE_WITH_PICKAXE) || e.getState().hasBlockEntity())return;var hit=HITS.get(p.getUUID());Direction face=hit!=null && hit.pos.equals(e.getPos()) && p.serverLevel().getGameTime()-hit.at<200?hit.face:Math.abs(p.getXRot())>45?Direction.UP:p.getDirection().getOpposite();JOBS.put(p.getUUID(),new Job(p,e.getPos().immutable(),e.getState(),face,p.getMainHandItem().getItem()));});
  NeoForge.EVENT_BUS.addListener((ServerTickEvent.Post e)->{for(var job:List.copyOf(JOBS.values())){JOBS.remove(job.player.getUUID());mine(job);} });
  NeoForge.EVENT_BUS.addListener((PlayerEvent.Clone e)->e.getEntity().getPersistentData().put(COOLDOWNS,e.getOriginal().getPersistentData().getCompound(COOLDOWNS).copy()));
  NeoForge.EVENT_BUS.addListener((PlayerEvent.PlayerLoggedInEvent e)->{if(e.getEntity() instanceof ServerPlayer p)syncCooldowns(p);});
  NeoForge.EVENT_BUS.addListener((net.neoforged.neoforge.event.server.ServerStoppedEvent e)->{HITS.clear();JOBS.clear();MINING.clear();});
 }
 private static void mine(Job j){var p=j.player;var world=p.serverLevel();if(!p.isAlive() || p.isRemoved() || p.getMainHandItem().getItem()!=j.item || world.getBlockState(j.pos).is(j.before.getBlock()))return;MINING.add(p.getUUID());try{for(var offset:MiningPlane.offsets(j.face.getAxis().name())){if(p.getMainHandItem().getItem()!=j.item)break;var pos=j.pos.offset(offset[0],offset[1],offset[2]);var state=world.getBlockState(pos);if(world.getChunkSource().hasChunk(pos.getX()>>4,pos.getZ()>>4) && state.is(BlockTags.MINEABLE_WITH_PICKAXE) && !state.hasBlockEntity() && state.getDestroySpeed(world,pos)>=0 && (p.isCreative() || state.canHarvestBlock(world,pos,p)))p.gameMode.destroyBlock(pos);}}finally{MINING.remove(p.getUUID());}}
 public static final class FlavorKey extends Item {public FlavorKey(Properties p){super(p);}@Override public Component getName(ItemStack s){return WeaponFlavor.name(s);}@Override public void appendHoverText(ItemStack s,TooltipContext c,List<Component> lines,TooltipFlag f){WeaponFlavor.append(s,lines,0);}}
 public static final class Hammer extends PickaxeItem {public Hammer(Properties p){super(Tiers.DIAMOND,p);} @Override public Component getName(ItemStack s){return WeaponFlavor.name(s);}@Override public void appendHoverText(ItemStack s,TooltipContext c,List<Component> lines,TooltipFlag f){WeaponFlavor.append(s,lines,0);}}
 public static final class FlavorAxe extends AxeItem {public FlavorAxe(Properties p){super(Tiers.DIAMOND,p);}@Override public Component getName(ItemStack s){return WeaponFlavor.name(s);}@Override public void appendHoverText(ItemStack s,TooltipContext c,List<Component> lines,TooltipFlag f){WeaponFlavor.append(s,lines,0);}}
 public static final class FlavorPickaxe extends PickaxeItem {public FlavorPickaxe(Properties p){super(Tiers.DIAMOND,p);}@Override public Component getName(ItemStack s){return WeaponFlavor.name(s);}@Override public void appendHoverText(ItemStack s,TooltipContext c,List<Component> lines,TooltipFlag f){WeaponFlavor.append(s,lines,0);}}
 public static final class Hat extends Item implements Equipable {public Hat(Properties p){super(p);}public EquipmentSlot getEquipmentSlot(){return EquipmentSlot.HEAD;}@Override public InteractionResultHolder<ItemStack> use(Level l,Player p,InteractionHand h){return swapWithEquipmentSlot(this,l,p,h);}}
 public static final class AbilitySword extends SwordItem {
  public final Power power;public AbilitySword(Tier t,Properties p,Power power){super(t,p);this.power=power;}
  @Override public InteractionResultHolder<ItemStack> use(Level level,Player player,InteractionHand hand){var stack=player.getItemInHand(hand);if(power==Power.NONE)return InteractionResultHolder.pass(stack);if(level.isClientSide)return InteractionResultHolder.success(stack);return player instanceof ServerPlayer p && activate(power,p,stack,hand,true)?InteractionResultHolder.success(stack):InteractionResultHolder.fail(stack);}
  @Override public void postHurtEnemy(ItemStack stack,LivingEntity target,LivingEntity attacker){super.postHurtEnemy(stack,target,attacker);if(power==Power.WITHER && !target.level().isClientSide && target.isAlive())target.addEffect(new MobEffectInstance(MobEffects.WITHER,100,0),attacker);}
  @Override public Component getName(ItemStack s){return WeaponFlavor.name(s);}@Override public void appendHoverText(ItemStack s,TooltipContext c,List<Component> lines,TooltipFlag f){WeaponFlavor.append(s,lines,power.seconds);}
 }
 public static final DeferredHolder<EntityType<?>,EntityType<TideProjectile>> TIDE_PROJECTILE=ENTITIES.register("tide_projectile",()->EntityType.Builder.<TideProjectile>of(TideProjectile::new,MobCategory.MISC).sized(0.5f,0.5f).clientTrackingRange(8).updateInterval(1).build("zianmanager:tide_projectile"));
 public static final class TideTrident extends TridentItem {
  public TideTrident(Properties properties){super(properties);}
  @Override public InteractionResultHolder<ItemStack> use(Level level,Player player,InteractionHand hand){if(player instanceof ServerPlayer p)activate(Power.TIDE,p,player.getItemInHand(hand),hand,false);return super.use(level,player,hand);}
  @Override public Component getName(ItemStack s){return WeaponFlavor.name(s);}@Override public void appendHoverText(ItemStack s,TooltipContext c,List<Component> lines,TooltipFlag f){WeaponFlavor.append(s,lines,Power.TIDE.seconds);}
 }
 public static final class TideProjectile extends net.minecraft.world.entity.projectile.ThrownTrident {
  public TideProjectile(EntityType<? extends TideProjectile> type,Level level){super(type,level);}
  @Override protected ItemStack getDefaultPickupItem(){return new ItemStack(item("altar_tide"));}
 }
 private static boolean activate(Power power,ServerPlayer p,ItemStack stack,InteractionHand hand,boolean visual){long now=System.currentTimeMillis(),wait=p.getPersistentData().getCompound(COOLDOWNS).getLong(power.name())-now;if(wait>0){if(visual){p.getCooldowns().addCooldown(stack.getItem(),(int)((wait+49)/50));p.displayClientMessage(Component.literal("Disponible en "+((wait+999)/1000)+" segundos"),true);}return false;}var data=p.getPersistentData().getCompound(COOLDOWNS);data.putLong(power.name(),now+power.seconds*1000L);p.getPersistentData().put(COOLDOWNS,data);if(visual)p.getCooldowns().addCooldown(stack.getItem(),power.seconds*20);p.server.getPlayerList().save(p);
  if(power==Power.FLAME){var bolt=BOLT.get().create(p.serverLevel());if(bolt==null)return false;bolt.setOwner(p);bolt.setPos(p.getEyePosition().add(p.getLookAngle().scale(0.7)));bolt.setDeltaMovement(p.getLookAngle().scale(1.2));p.serverLevel().addFreshEntity(bolt);p.serverLevel().playSound(null,p.blockPosition(),SoundEvents.BLAZE_SHOOT,SoundSource.PLAYERS,1,1);}
  else if(power==Power.SONIC)sonic(p);
  else if(power==Power.GLADIATOR){effect(p,MobEffects.REGENERATION,0);effect(p,MobEffects.ABSORPTION,0);effect(p,MobEffects.DAMAGE_BOOST,0);}
  else if(power==Power.REGEN){effect(p,MobEffects.REGENERATION,0);effect(p,MobEffects.ABSORPTION,4);}
  else if(power==Power.DRAGON){effect(p,MobEffects.DAMAGE_BOOST,0);effect(p,MobEffects.ABSORPTION,0);}
  else if(power==Power.WITHER)effect(p,MobEffects.DAMAGE_BOOST,1);
  else if(power==Power.NIGHT){effect(p,MobEffects.REGENERATION,0);effect(p,MobEffects.ABSORPTION,1);}
  else if(power==Power.WARRIOR){effect(p,MobEffects.DAMAGE_BOOST,1);effect(p,MobEffects.DAMAGE_RESISTANCE,0);}
  else if(power==Power.NECROMANCER){effect(p,MobEffects.REGENERATION,1);effect(p,MobEffects.ABSORPTION,1);}
  else if(power==Power.NINJA){effect(p,MobEffects.DAMAGE_BOOST,0);effect(p,MobEffects.REGENERATION,0);}
  else if(power==Power.CHAINSAW){effect(p,MobEffects.DAMAGE_BOOST,1);effect(p,MobEffects.DAMAGE_RESISTANCE,1);}
  else if(power==Power.TIDE){effect(p,MobEffects.REGENERATION,0);effect(p,MobEffects.DAMAGE_BOOST,0);}
  stack.hurtAndBreak(1,p,hand==InteractionHand.MAIN_HAND?EquipmentSlot.MAINHAND:EquipmentSlot.OFFHAND);return true;
 }
 private static void effect(ServerPlayer p,Holder<net.minecraft.world.effect.MobEffect> effect,int amplifier){p.addEffect(new MobEffectInstance(effect,600,amplifier));}
 private static void syncCooldowns(ServerPlayer p){long now=System.currentTimeMillis();for(var ref:ALL.values())if(ref.get() instanceof AbilitySword item){long wait=p.getPersistentData().getCompound(COOLDOWNS).getLong(item.power.name())-now;if(wait>0)p.getCooldowns().addCooldown(item,(int)Math.min(Integer.MAX_VALUE,(wait+49)/50));}}
 public static void sonic(ServerPlayer p){var start=p.getEyePosition();var direction=p.getLookAngle().normalize();LivingEntity target=null;double nearest=16;for(var e:p.serverLevel().getEntitiesOfClass(LivingEntity.class,p.getBoundingBox().expandTowards(direction.scale(15)).inflate(1.5),e->e!=p && e.isAlive() && !e.isSpectator())){double along=e.getBoundingBox().getCenter().subtract(start).dot(direction);if(along<0 || along>15 || along>=nearest || !e.getBoundingBox().inflate(0.5).clip(start,start.add(direction.scale(15))).isPresent())continue;if(e instanceof Player other && (!p.server.isPvpAllowed() || !p.canHarmPlayer(other)))continue;target=e;nearest=along;}for(int i=1;i<=15;i++){var v=start.add(direction.scale(i));p.serverLevel().sendParticles(ParticleTypes.SONIC_BOOM,v.x,v.y,v.z,1,0,0,0,0);}p.serverLevel().playSound(null,p.blockPosition(),SoundEvents.WARDEN_SONIC_BOOM,SoundSource.PLAYERS,1,1);if(target!=null && target.hurt(p.serverLevel().damageSources().sonicBoom(p),10)){double resistance=target.getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.KNOCKBACK_RESISTANCE);target.push(direction.x*2.5*(1-resistance),0.5*(1-resistance),direction.z*2.5*(1-resistance));}}
 public static final class FlameBolt extends SmallFireball {public FlameBolt(EntityType<? extends FlameBolt> type,Level level){super(type,level);} @Override protected void onHitBlock(BlockHitResult hit){discard();} @Override public void tick(){super.tick();if(tickCount>100)discard();}}
}
