package com.ianblk.zianmanager;

import java.time.*;
import java.util.UUID;
import java.util.*;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.*;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.vault.*;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/** Runs only in an explicitly enabled disposable localhost world. */
public final class VaultCooldownSmoke {
 public record RestartMarker(UUID player,long remaining){}
 private boolean done;
 public VaultCooldownSmoke(){if("true".equals(System.getenv("ZIANMANAGER_VAULT_SMOKE")))NeoForge.EVENT_BUS.addListener(this::run);}
 private static void check(boolean value,String message){if(!value)throw new IllegalStateException(message);}
 private static void finish(net.minecraft.server.level.ServerLevel level,BlockPos pos,VaultBlockEntity vault){
  var box=new net.minecraft.world.phys.AABB(pos).inflate(6);
  int before=level.getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class,box).stream().mapToInt(e->e.getItem().getCount()).sum();
  int expected=vault.getServerData().getItemsToEject().stream().mapToInt(ItemStack::getCount).sum();
  for(int i=0;i<300 && (level.getBlockState(pos).getValue(VaultBlock.STATE)==VaultState.UNLOCKING || level.getBlockState(pos).getValue(VaultBlock.STATE)==VaultState.EJECTING);i++){
   ((net.minecraft.world.level.storage.ServerLevelData)level.getLevelData()).setGameTime(level.getGameTime()+1);
   VaultBlockEntity.Server.tick(level,pos,level.getBlockState(pos),vault.getConfig(),vault.getServerData(),vault.getSharedData());
  }
  check(vault.getServerData().getItemsToEject().isEmpty(),"Native loot finished ejecting");
  int after=level.getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class,box).stream().mapToInt(e->e.getItem().getCount()).sum();
  check(after-before==expected,"Exactly the queued native loot ejected");
 }
 private void run(ServerTickEvent.Post event){if(done)return;done=true;var server=event.getServer();
  try{
   check("127.0.0.1".equals(server.getLocalIp()),"Vault smoke requires localhost");
   var level=server.overworld();var runtime=VaultCooldownRuntime.INSTANCE;long now=1800000000000L;
   server.getCommands().performPrefixedCommand(server.createCommandSourceStack(),"zianmanager vaults times 30 60");
   runtime.testClock(Clock.fixed(Instant.ofEpochMilli(now),ZoneOffset.UTC));
   var alice=FakePlayerFactory.get(level,new GameProfile(UUID.randomUUID(),"VaultAlice"));alice.setGameMode(GameType.SURVIVAL);
   var bob=FakePlayerFactory.get(level,new GameProfile(UUID.randomUUID(),"VaultBob"));bob.setGameMode(GameType.SURVIVAL);
   var pos=new BlockPos(0,100,0);level.getChunkAt(pos);
   var markerPath=server.getWorldPath(net.minecraft.world.level.storage.LevelResource.ROOT).resolve("data/zianmanager/vault-smoke-restart.json");
   var marker=com.ianblk.zianmanager.core.AtomicJson.read(markerPath,RestartMarker.class,null);
   var identity=new com.ianblk.zianmanager.core.VaultCooldownLedger.Vault("minecraft:overworld",0,100,0);
   if(marker!=null){
    check(runtime.testLedger().remaining(identity,marker.player(),now)==marker.remaining(),"Real server restart retains deadline");
    check(level.getBlockEntity(pos) instanceof VaultBlockEntity saved && saved.getServerData().getRewardedPlayers().contains(marker.player()),"Native UUID survives world save/reload");
    ZianManager.LOGGER.info("ZIAN_VAULT_RESTART_PASS: native NBT and durable cooldown unchanged after actual server restart");
   }
   var active=Blocks.VAULT.defaultBlockState().setValue(VaultBlock.STATE,VaultState.ACTIVE);
   level.setBlock(pos,active,3);var vault=(VaultBlockEntity)level.getBlockEntity(pos);
   vault.setConfig(new VaultConfig(net.minecraft.world.level.storage.loot.BuiltInLootTables.TRIAL_CHAMBERS_REWARD,4,4.5,new ItemStack(Items.TRIAL_KEY),Optional.empty()));
   var keys=new ItemStack(Items.TRIAL_KEY,4);
   VaultBlockEntity.Server.tryInsertKey(level,pos,active,vault.getConfig(),vault.getServerData(),vault.getSharedData(),alice,keys);
   check(keys.getCount()==3,"Native key consumption");check(level.getBlockState(pos).getValue(VaultBlock.STATE)==VaultState.UNLOCKING,"Native opening and animation");
   check(!vault.getSharedData().getDisplayItem().isEmpty(),"Native loot queued");
   VaultBlockEntity.Server.tryInsertKey(level,pos,level.getBlockState(pos),vault.getConfig(),vault.getServerData(),vault.getSharedData(),alice,keys);
   check(keys.getCount()==3,"Busy vault cannot accept another key");
   finish(level,pos,vault);
   level.setBlock(pos,active,3);
   VaultBlockEntity.Server.tryInsertKey(level,pos,active,vault.getConfig(),vault.getServerData(),vault.getSharedData(),alice,keys);
   check(keys.getCount()==3,"Early retry consumes no key");
   var bobKeys=new ItemStack(Items.TRIAL_KEY,2);
   VaultBlockEntity.Server.tryInsertKey(level,pos,active,vault.getConfig(),vault.getServerData(),vault.getSharedData(),bob,bobKeys);
   check(bobKeys.getCount()==1,"Independent second player");
   finish(level,pos,vault);
   runtime.testClock(Clock.fixed(Instant.ofEpochMilli(now+1800000),ZoneOffset.UTC));level.setBlock(pos,active,3);
   VaultBlockEntity.Server.tryInsertKey(level,pos,active,vault.getConfig(),vault.getServerData(),vault.getSharedData(),alice,keys);
   check(keys.getCount()==2,"Same player can reopen after cooldown");
   finish(level,pos,vault);
   // Native activation uses the rewarded UUID set directly. Expired players must still activate the vault.
   var base=vault.getConfig();vault.setConfig(new VaultConfig(base.lootTable(),4,4.5,base.keyItem(),Optional.empty(),(l,s,p,r,los)->List.of(alice.getUUID()),base.entitySelector()));
   level.setBlock(pos,active.setValue(VaultBlock.STATE,VaultState.INACTIVE),3);
   runtime.testClock(Clock.fixed(Instant.ofEpochMilli(now+3600000),ZoneOffset.UTC));
   for(int i=0;i<30;i++){((net.minecraft.world.level.storage.ServerLevelData)level.getLevelData()).setGameTime(level.getGameTime()+1);VaultBlockEntity.Server.tick(level,pos,level.getBlockState(pos),vault.getConfig(),vault.getServerData(),vault.getSharedData());}
   check(level.getBlockState(pos).getValue(VaultBlock.STATE)==VaultState.ACTIVE,"Expired UUID reactivates native vault");
   check(vault.getServerData().getRewardedPlayers().contains(alice.getUUID()),"Detection preserves native historical UUIDs");
   var wrong=new ItemStack(Items.OMINOUS_TRIAL_KEY,2);level.setBlock(pos,active,3);
   VaultBlockEntity.Server.tryInsertKey(level,pos,active,vault.getConfig(),vault.getServerData(),vault.getSharedData(),FakePlayerFactory.get(level,new GameProfile(UUID.randomUUID(),"VaultWrong")),wrong);
   check(wrong.getCount()==2,"Wrong key untouched");
   // Legacy redemption starts a wait; clearing or evicting the native set cannot bypass that wait.
   var legacy=FakePlayerFactory.get(level,new GameProfile(UUID.randomUUID(),"VaultLegacy"));vault.getServerData().addToRewardedPlayers(legacy);
   var legacyKeys=new ItemStack(Items.TRIAL_KEY,2);
   level.setBlock(pos,active.setValue(VaultBlock.STATE,VaultState.INACTIVE),3);legacy.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND,legacyKeys);
   var click=new net.neoforged.neoforge.event.entity.player.PlayerInteractEvent.RightClickBlock(legacy,net.minecraft.world.InteractionHand.MAIN_HAND,pos,new net.minecraft.world.phys.BlockHitResult(pos.getCenter(),net.minecraft.core.Direction.NORTH,pos,false));
   NeoForge.EVENT_BUS.post(click);check(click.isCanceled(),"Legacy inactive vault starts cooldown on actual interaction event");
   level.setBlock(pos,active,3);
   VaultBlockEntity.Server.tryInsertKey(level,pos,active,vault.getConfig(),vault.getServerData(),vault.getSharedData(),legacy,legacyKeys);
   vault.getServerData().getRewardedPlayers().remove(legacy.getUUID());
   VaultBlockEntity.Server.tryInsertKey(level,pos,active,vault.getConfig(),vault.getServerData(),vault.getSharedData(),legacy,legacyKeys);
   check(legacyKeys.getCount()==2,"Legacy and UUID eviction cannot bypass cooldown");
   // Non-empty table with no generated rewards rolls back the reservation and consumes no key.
   var emptyPlayer=FakePlayerFactory.get(level,new GameProfile(UUID.randomUUID(),"VaultEmpty"));
   vault.setConfig(new VaultConfig(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.LOOT_TABLE,net.minecraft.resources.ResourceLocation.parse("zianmanager:missing_smoke_table")),4,4.5,new ItemStack(Items.TRIAL_KEY),Optional.empty()));
   VaultBlockEntity.Server.tryInsertKey(level,pos,active,vault.getConfig(),vault.getServerData(),vault.getSharedData(),emptyPlayer,legacyKeys);
   check(legacyKeys.getCount()==2,"Empty reward consumes no key");
   var id=new com.ianblk.zianmanager.core.VaultCooldownLedger.Vault("minecraft:overworld",pos.getX(),pos.getY(),pos.getZ());
   check(!runtime.testLedger().known(id,emptyPlayer.getUUID()),"Empty reward reservation rolled back");
   var ominousPos=pos.offset(4,0,0);var ominousState=active.setValue(VaultBlock.OMINOUS,true);level.setBlock(ominousPos,ominousState,3);
   var ominous=(VaultBlockEntity)level.getBlockEntity(ominousPos);ominous.setConfig(new VaultConfig(net.minecraft.world.level.storage.loot.BuiltInLootTables.TRIAL_CHAMBERS_REWARD_OMINOUS,4,4.5,new ItemStack(Items.OMINOUS_TRIAL_KEY),Optional.empty()));
   var ominousKeys=new ItemStack(Items.OMINOUS_TRIAL_KEY,2);
   VaultBlockEntity.Server.tryInsertKey(level,ominousPos,ominousState,ominous.getConfig(),ominous.getServerData(),ominous.getSharedData(),alice,ominousKeys);
   check(ominousKeys.getCount()==1,"Ominous key accepted");finish(level,ominousPos,ominous);
   var ominousId=new com.ianblk.zianmanager.core.VaultCooldownLedger.Vault("minecraft:overworld",4,100,0);
   check(runtime.testLedger().remaining(ominousId,alice.getUUID(),now+3600000)==3600000,"Separate 60 minute ominous cooldown");
   server.getCommands().performPrefixedCommand(server.createCommandSourceStack(),"zianmanager vaults times 1 2");
   check(runtime.testLedger().remaining(ominousId,alice.getUUID(),now+3600000)==3600000,"Administrative change preserves existing deadlines");
   var dispatcher=server.getCommands().getDispatcher();var console=server.createCommandSourceStack();
   check(dispatcher.execute("zianmanager vaults times normal 1",console)==1,"Named normal command accepted");
   check(runtime.testSettings().normalMinutes()==1 && runtime.testSettings().ominousMinutes()==2,"Named normal leaves ominous unchanged");
   check(dispatcher.execute("zianmanager vaults times ominous 1",console)==1,"Named ominous command accepted");
   check(runtime.testSettings().ominousMinutes()==1,"Named ominous changed");
   check(dispatcher.execute("zianmanager vaults times 1 ominous",console)==1,"Original player syntax accepted as alias");
   check(dispatcher.execute("zianmanager vaults",console)==1,"Bare vault command displays help");
   var kinds=dispatcher.getCompletionSuggestions(dispatcher.parse("zianmanager vaults times ",console)).join().getList().stream().map(s->s.getText()).toList();
   check(kinds.contains("normal") && kinds.contains("ominous"),"Tab suggests both named vault types");
   var numbers=dispatcher.getCompletionSuggestions(dispatcher.parse("zianmanager vaults times ominous ",console)).join().getList().stream().map(s->s.getText()).toList();
   check(numbers.contains("1") && numbers.contains("60"),"Tab suggests minutes");
   alice.setPos(pos.getX()+0.5,pos.getY()+3,pos.getZ()+0.5);alice.setXRot(90);alice.setYRot(0);
   // Youer resolves permission checks through the original Bukkit sender; a FakePlayer has none.
   var playerSource=console.withEntity(alice).withPermission(4);
   check(dispatcher.execute("zianmanager vaults cooldown 1",playerSource)==1,"Looked-at vault adjustment command accepted");
   check(runtime.testLedger().remaining(identity,alice.getUUID(),now+3600000)==60000,"Existing wait shortened to one minute");
   check(runtime.testLedger().remaining(ominousId,alice.getUUID(),now+3600000)==3600000,"Adjustment preserves other vault waits");
   check(dispatcher.execute("zianmanager vaults reset",playerSource)==1,"Explicit administrator reset accepted");
   check(runtime.testLedger().remaining(identity,alice.getUUID(),now+3600000)==0 && runtime.testLedger().known(identity,alice.getUUID()),"Reset preserves native adoption history");
   boolean playerRequired=false;try{dispatcher.execute("zianmanager vaults cooldown 1",console);}catch(com.mojang.brigadier.exceptions.CommandSyntaxException expected){playerRequired=true;}
   check(playerRequired,"Console cannot adjust an aimed vault without a player");
   server.getCommands().performPrefixedCommand(server.createCommandSourceStack(),"zianmanager vaults enabled false");
   check(ominous.getServerData().getRewardedPlayers().contains(alice.getUUID()),"Disable preserves vanilla historical redemption");
   server.getCommands().performPrefixedCommand(server.createCommandSourceStack(),"zianmanager vaults enabled true");
   server.getCommands().performPrefixedCommand(server.createCommandSourceStack(),"zianmanager vaults times 30 60");
   com.ianblk.zianmanager.core.AtomicJson.write(markerPath,new RestartMarker(alice.getUUID(),runtime.testLedger().remaining(identity,alice.getUUID(),now)));
   runtime.testClock(Clock.systemUTC());
   ZianManager.LOGGER.info("ZIAN_VAULT_SMOKE_PASS: exact native loot/key/animation, early/busy retry denied, independent players, timed reuse and reactivation, wrong keys, legacy adoption, UUID eviction guard, empty loot rollback, ominous 60min");
   server.halt(false);
  }catch(Throwable error){ZianManager.LOGGER.error("ZIAN_VAULT_SMOKE_FAIL",error);server.halt(false);}
 }
}
