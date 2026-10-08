package com.ianblk.zianmanager.reward;
import com.ianblk.zianmanager.core.*;
import com.ianblk.zianmanager.core.Definitions.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.*;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.network.chat.Component;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.storage.loot.*;
import net.minecraft.world.level.storage.loot.parameters.*;
import net.minecraft.world.phys.Vec3;
import java.nio.file.*;
import java.util.*;

/** Adapted from ZianRCT's durable intent and inventory readback delivery. */
public final class LootService {
    private final MinecraftServer server;private final ManagerStore store;private final RewardJournal journal;
    public LootService(MinecraftServer server,ManagerStore store) throws java.io.IOException{
        this.server=server;this.store=store;journal=RewardJournal.open(server.getWorldPath(LevelResource.ROOT).resolve("data/zianmanager/claims.json"));
    }
    public long remaining(ServerPlayer player,String key,int minutes){
        var last=journal.latest(player.getUUID(),key);if(last==null)return 0;if(!last.complete())return -1;
        return Math.max(0,last.nextEligibleAt()-System.currentTimeMillis());
    }
    public List<ItemStack> roll(ServerPlayer player,String preset,BlockPos pos){
        var definition=store.data().loot().get(preset);if(definition==null)throw new IllegalArgumentException("Loot table Zian inexistente: "+preset);
        var random=new java.security.SecureRandom();var out=new ArrayList<ItemStack>();
        if(!definition.table().isEmpty()){
            var key=ResourceKey.create(Registries.LOOT_TABLE,ResourceLocation.parse(definition.table()));
            var table=server.reloadableRegistries().getLootTable(key);
            var params=new LootParams.Builder(player.serverLevel()).withParameter(LootContextParams.ORIGIN,Vec3.atCenterOf(pos)).withOptionalParameter(LootContextParams.THIS_ENTITY,player).withLuck(player.getLuck()).create(LootContextParamSets.CHEST);
            out.addAll(table.getRandomItems(params));Collections.shuffle(out,random);if(out.size()>definition.rolls())out.subList(definition.rolls(),out.size()).clear();
        }else for(var entry:WeightedLoot.select(definition.entries(),definition.rolls(),random)){
            var stack=item(player,entry.item());stack.setCount(entry.min()+random.nextInt(entry.max()-entry.min()+1));
            if(stack.isEmpty() || stack.getCount()>stack.getMaxStackSize())throw new IllegalArgumentException("Cantidad incompatible con el objeto");out.add(stack);
        }
        if(out.isEmpty())throw new IllegalArgumentException("La tabla no produjo objetos. Revisa su configuración.");return out;
    }
    public RewardClaim reserve(ServerPlayer player,String key,String preset,int minutes,BlockPos pos) throws Exception{
        var old=journal.latest(player.getUUID(),key);if(old!=null && !old.complete())return old;
        if(old!=null && (minutes==0 || remaining(player,key,minutes)>0))return old;
        List<String> items=roll(player,preset,pos).stream().map(s->s.save(player.registryAccess()).toString()).toList();
        var definition=new RewardDefinition("",0,items,minutes==0?RewardDefinition.Mode.UNIQUE:RewardDefinition.Mode.REPEAT,minutes);
        journal.reserveAt(player.getUUID(),key,definition,System.currentTimeMillis(),UUID.randomUUID());return journal.latest(player.getUUID(),key);
    }
    public void grant(ServerPlayer player,String key,String preset,int minutes,BlockPos pos) throws Exception{
        if(!com.ianblk.zianmanager.permission.ManagerPermissions.allows(player.createCommandSourceStack(),"loot",false))return;
        if(minutes>0 && remaining(player,key,minutes)>0){player.sendSystemMessage(Component.literal("Próximo loot en "+((remaining(player,key,minutes)+59999)/60000)+" minuto(s)."));return;}
        var claim=reserve(player,key,preset,minutes,pos);deliver(player,claim.id());
    }
    public List<RewardClaim> pending(UUID uuid){return journal.forPlayer(uuid).stream().filter(c->!c.complete()).toList();}
    public void deliver(ServerPlayer player,UUID id) throws Exception{
        if(!com.ianblk.zianmanager.permission.ManagerPermissions.allows(player.createCommandSourceStack(),"loot",false))throw new IllegalArgumentException("No tienes permiso para recibir loot Zian");
        RewardDelivery.deliver(journal,player.getUUID(),id,new RewardDelivery.Port(){
            public String unavailable(RewardClaim.Part part){
                if(!player.isAlive() || player.isRemoved())return "player_unavailable";
                try{var stack=item(player,part.data());return stack.isEmpty()?"item_unavailable":!fits(player,stack)?"inventory_full":null;}catch(Exception error){return "invalid_item";}
            }
            public RewardDelivery.Result apply(RewardClaim.Part part) throws Exception{
                var stack=item(player,part.data());player.getInventory().add(stack);if(!stack.isEmpty())return RewardDelivery.Result.UNCERTAIN;
                player.getInventory().setChanged();player.inventoryMenu.broadcastChanges();
                var expected=player.saveWithoutId(new CompoundTag()).get("Inventory");server.getPlayerList().save(player);
                Path saved=server.getWorldPath(LevelResource.PLAYER_DATA_DIR).resolve(player.getUUID()+".dat");
                try(var channel=java.nio.channels.FileChannel.open(saved,StandardOpenOption.WRITE)){channel.force(true);}
                var read=NbtIo.readCompressed(saved,NbtAccounter.create(8L*1024*1024));
                return expected!=null && expected.equals(read.get("Inventory"))?RewardDelivery.Result.APPLIED:RewardDelivery.Result.UNCERTAIN;
            }
        });
        var claim=journal.get(id);player.sendSystemMessage(Component.literal(claim.complete()?"Loot entregado.":claim.review()?"Entrega en revisión. No se repetirá automáticamente.":"Loot pendiente. Libera espacio y usa /zianmanager pending."));
    }
    public void resolve(UUID player,UUID id,int part,UUID admin,String evidence) throws Exception{journal.resolve(player,id,part,admin,"confirmed_compensated",evidence);}
    public static ItemStack item(ServerPlayer player,String data){try{return ItemStack.parseOptional(player.registryAccess(),TagParser.parseTag(data));}catch(Exception e){throw new IllegalArgumentException("Objeto ilegible",e);}}
    private static boolean fits(ServerPlayer player,ItemStack stack){int space=0;for(int i=0;i<36;i++){var slot=player.getInventory().getItem(i);space+=slot.isEmpty()?stack.getMaxStackSize():ItemStack.isSameItemSameComponents(slot,stack)?Math.max(0,slot.getMaxStackSize()-slot.getCount()):0;if(space>=stack.getCount())return true;}return false;}
}
