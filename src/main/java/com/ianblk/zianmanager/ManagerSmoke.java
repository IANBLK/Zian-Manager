package com.ianblk.zianmanager;
import com.ianblk.zianmanager.core.Definitions.*;
import com.ianblk.zianmanager.core.EncounterLedger;
import com.ianblk.zianmanager.core.EncounterLedger.Phase;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.*;
import net.minecraft.tags.TagKey;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import com.mojang.authlib.GameProfile;
import java.nio.file.*;
import java.util.*;

/** Opt-in localhost integration fixtures; never runs in normal servers. */
public final class ManagerSmoke {
    private int phase,ticks;private boolean done;private ServerPlayer first,second;private UUID originalRun;private BlockPos chest;private long spawned;
    private UUID firstId,secondId;
    private static final UUID FIRST=UUID.fromString("00000000-0000-0000-0000-000000000111"),SECOND=UUID.fromString("00000000-0000-0000-0000-000000000112"),CHEST=UUID.fromString("00000000-0000-0000-0000-000000000113"),NPC=UUID.fromString("00000000-0000-0000-0000-000000000114");
    public ManagerSmoke(){if(!"true".equals(System.getenv("ZIANMANAGER_SMOKE")))return;NeoForge.EVENT_BUS.addListener(this::tick);}
    private void tick(ServerTickEvent.Post event){if(done)return;var server=event.getServer();try{
        if(!"127.0.0.1".equals(server.getLocalIp()))throw new IllegalStateException("Integration fixtures require localhost");
        var runtime=ManagerRuntime.get();var store=runtime.store();ServerLevel level=server.overworld();Path marker=server.getWorldPath(LevelResource.ROOT).resolve("data/zianmanager/smoke.marker");
        if(phase==0){
            var saved=Files.exists(marker)?Files.readString(marker).split("\n"):null;
            firstId=saved==null?UUID.randomUUID():UUID.fromString(saved[0]);secondId=saved==null?UUID.randomUUID():UUID.fromString(saved[1]);
            first=FakePlayerFactory.get(level,new GameProfile(firstId,"ZianMgrSmoke1"));second=FakePlayerFactory.get(level,new GameProfile(secondId,"ZianMgrSmoke2"));
            var channelField=net.minecraft.network.Connection.class.getDeclaredField("channel");channelField.setAccessible(true);
            channelField.set(first.connection.getConnection(),new io.netty.channel.embedded.EmbeddedChannel());channelField.set(second.connection.getConnection(),new io.netty.channel.embedded.EmbeddedChannel());
            var byUuid=net.minecraft.server.players.PlayerList.class.getDeclaredField("playersByUUID");byUuid.setAccessible(true);@SuppressWarnings("unchecked") var lookup=(Map<UUID,ServerPlayer>)byUuid.get(server.getPlayerList());lookup.put(firstId,first);lookup.put(secondId,second);
            var playerList=net.minecraft.server.players.PlayerList.class.getDeclaredField("players");playerList.setAccessible(true);@SuppressWarnings("unchecked") var players=(List<ServerPlayer>)playerList.get(server.getPlayerList());players.add(first);players.add(second);
            var spawn=level.getSharedSpawnPos();int x=spawn.getX(),y=spawn.getY()+2,z=spawn.getZ();chest=new BlockPos(x+8,y,z);
            first.moveTo(x,y,z,0,0);second.moveTo(x+1,y,z,0,0);first.gameMode.changeGameModeForPlayer(GameType.SURVIVAL);second.gameMode.changeGameModeForPlayer(GameType.SURVIVAL);
            if(Files.exists(marker)){
                var c=store.data().chests().get(CHEST);if(c==null || !level.getBlockState(new BlockPos(c.x(),c.y(),c.z())).is(ManagerBlocks.CHEST.get()))throw new IllegalStateException("Chest missing after restart");
                if(runtime.loot().remaining(first,"chest."+CHEST,10)<=0 || !runtime.loot().pending(firstId).isEmpty())throw new IllegalStateException("Personal cooldown/receipt missing after restart");
                if(store.data().npcs().get(NPC)==null || level.getEntity(NPC)==null)throw new IllegalStateException("Dialogue NPC missing after restart");
                ZianManager.LOGGER.info("Zian Manager native smoke passed: restart preserves chest, NPC, personal loot and cooldown; Lootr={}",net.neoforged.fml.ModList.get().isLoaded("lootr"));done=true;return;
            }
            runtime.cancel("smoke_room");
            List<net.minecraft.world.item.Item> items=List.of(Items.DIAMOND,Items.EMERALD,Items.IRON_INGOT,Items.GOLD_INGOT,Items.REDSTONE,Items.COAL,Items.LAPIS_LAZULI,Items.TORCH,Items.COBBLESTONE,Items.BREAD,Items.APPLE,Items.CARROT,Items.POTATO,Items.STICK,Items.STRING,Items.ARROW,Items.BONE,Items.WHEAT,Items.LEATHER,Items.AMETHYST_SHARD);
            var entries=new ArrayList<LootEntry>();for(var item:items)entries.add(new LootEntry(new ItemStack(item).save(server.registryAccess()).toString(),1,1,1));
            store.put(new LootSpec("smoke_chest","CHEST",10,entries,""));store.put(new LootSpec("smoke_mob","MOB",2,entries.subList(0,10),""));store.put(new LootSpec("smoke_boss","BOSS",5,entries.subList(0,10),""));
            var guard=new MobSpec("smoke_guard","Guardián de prueba","minecraft:zombie",40,6,2,1,0.2,List.of(new Effect("minecraft:resistance",0,120)),Map.of("head",new ItemStack(Items.IRON_HELMET).save(server.registryAccess()).toString()),"smoke_mob",false);store.put(guard);
            for(int px=x-2;px<x+12;px++)for(int pz=z-2;pz<z+8;pz++){for(int py=y;py<y+4;py++)level.setBlockAndUpdate(new BlockPos(px,py,pz),net.minecraft.world.level.block.Blocks.AIR.defaultBlockState());level.setBlockAndUpdate(new BlockPos(px,y-1,pz),net.minecraft.world.level.block.Blocks.STONE.defaultBlockState());}
            level.setBlockAndUpdate(chest,ManagerBlocks.CHEST.get().defaultBlockState());store.put(new ChestSpec(CHEST,"minecraft:overworld",chest.getX(),chest.getY(),chest.getZ(),"smoke_chest",10));
            if(!level.getBlockState(chest).is(TagKey.create(Registries.BLOCK,ResourceLocation.parse("lootr:convert/blacklist"))))throw new IllegalStateException("Lootr block blacklist missing");
            if(!ManagerBlocks.CHEST_ENTITY.get().builtInRegistryHolder().is(TagKey.create(Registries.BLOCK_ENTITY_TYPE,ResourceLocation.parse("lootr:convert/blacklist"))))throw new IllegalStateException("Lootr block-entity blacklist missing");
            if(level.getBlockState(chest).getDestroySpeed(level,chest)!=-1 || ManagerBlocks.CHEST.get().getExplosionResistance()<1000000)throw new IllegalStateException("Chest durability protection missing");
            first.gameMode.changeGameModeForPlayer(GameType.CREATIVE);if(first.gameMode.destroyBlock(chest))throw new IllegalStateException("Creative player broke chest");first.gameMode.changeGameModeForPlayer(GameType.SURVIVAL);if(first.gameMode.destroyBlock(chest))throw new IllegalStateException("Survival player broke chest");
            if(server.getRecipeManager().getRecipes().stream().anyMatch(r->r.value().getResultItem(server.registryAccess()).is(ManagerBlocks.CHEST_ITEM.get())))throw new IllegalStateException("Chest has a crafting recipe");
            int before=first.getInventory().items.stream().mapToInt(ItemStack::getCount).sum();runtime.loot().grant(first,"chest."+CHEST,"smoke_chest",10,chest);int after=first.getInventory().items.stream().mapToInt(ItemStack::getCount).sum();if(after-before!=10 || !runtime.loot().pending(firstId).isEmpty())throw new IllegalStateException("Ten-item personal chest delivery failed");
            runtime.loot().grant(second,"chest."+CHEST,"smoke_chest",10,chest);runtime.loot().grant(first,"chest."+CHEST,"smoke_chest",10,chest);if(first.getInventory().items.stream().mapToInt(ItemStack::getCount).sum()!=after)throw new IllegalStateException("Personal chest duplicate payout");
            store.put(new NpcSpec(NPC,"minecraft:overworld",new Point(x+10,y,z,90),"Guía de prueba","Bienvenido a la dungeon."));
            var points=List.of(new Point(x+2.5,y,z+4.5,0),new Point(x+5.5,y,z+4.5,0),new Point(x+8.5,y,z+4.5,0));
            store.put(new ZoneSpec("smoke_room","minecraft:overworld",new Point(x-2,y,z-2,0),new Point(x+12,y+6,z+8,0),points,List.of("smoke_guard","smoke_guard","smoke_guard"),2,1,10,"smoke_boss",true));
            level.setBlockAndUpdate(chest,net.minecraft.world.level.block.Blocks.AIR.defaultBlockState());phase=1;ticks=0;return;
        }
        var run=runtime.encounter("smoke_room");
        if(phase==1 && ++ticks>40){
            if(!level.getBlockState(chest).is(ManagerBlocks.CHEST.get()))throw new IllegalStateException("Registered chest did not recover");
            if(run==null || run.phase()!=Phase.ACTIVE || run.spawns().size()!=3)throw new IllegalStateException("Entry did not create one shared three-mob encounter");originalRun=run.uuid();
            for(var slot:run.spawns()){var mob=(Mob)level.getEntity(slot.uuid());if(mob==null || mob.getMaxHealth()!=40 || mob.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.HEAD).isEmpty())throw new IllegalStateException("Configured mob attributes/equipment missing");mob.hurt(level.damageSources().playerAttack(first),1000);}
            phase=2;ticks=0;return;
        }
        if(phase==2 && ++ticks>40){if(run.wave()!=2 || run.phase()!=Phase.ACTIVE || !run.uuid().equals(originalRun))throw new IllegalStateException("Second wave missing or duplicate encounter");for(var slot:run.spawns())((Mob)level.getEntity(slot.uuid())).hurt(level.damageSources().playerAttack(first),1000);phase=3;ticks=0;return;}
        if(phase==3){if(run.phase()!=Phase.COMPLETE)throw new IllegalStateException("Zone did not complete");phase=4;ticks=0;return;}
        if(phase==4){if(run.uuid().equals(originalRun)){if(++ticks>400)throw new IllegalStateException("Cooldown did not spawn a fresh encounter");return;}if(run.phase()!=Phase.ACTIVE)throw new IllegalStateException("Respawn not active");runtime.cancel("smoke_room");if(!runtime.loot().pending(firstId).isEmpty())throw new IllegalStateException("Native loot remained unconfirmed");Files.writeString(marker,firstId+"\n"+secondId+"\n");ZianManager.LOGGER.info("Zian Manager native smoke passed: shared zone, 3-mob waves, respawn, item readback, personal chest, unbreakable/no recipe, dialogue NPC; Lootr={}",net.neoforged.fml.ModList.get().isLoaded("lootr"));done=true;}
    }catch(Exception error){done=true;ZianManager.LOGGER.error("Zian Manager native smoke FAILED",error);}}
}
