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
    private final MinecraftServer server;private final ManagerStore store;private final RewardJournal journal;private final com.ianblk.zianmanager.core.KeyLedger keys;
    public LootService(MinecraftServer server,ManagerStore store) throws java.io.IOException{
        this.server=server;this.store=store;keys=new com.ianblk.zianmanager.core.KeyLedger(server.getWorldPath(LevelResource.ROOT).resolve("data/zianmanager/keys.json"));journal=RewardJournal.open(server.getWorldPath(LevelResource.ROOT).resolve("data/zianmanager/claims.json"));
    }
    public long remaining(ServerPlayer player,String key,int minutes){
        var last=journal.latest(player.getUUID(),key);if(last==null)return 0;if(!last.complete())return -1;if(minutes==0 && !requiredKey(key).isEmpty())return 0;
        return Math.max(0,last.nextEligibleAt()-System.currentTimeMillis());
    }
    public List<ItemStack> roll(ServerPlayer player,String preset,BlockPos pos){
        var definition=store.data().loot().get(preset);if(definition==null)throw new IllegalArgumentException("Loot table Zian inexistente: "+preset);
        var random=new java.security.SecureRandom();var out=new ArrayList<ItemStack>();
        if(!definition.table().isEmpty()){
            var key=ResourceKey.create(Registries.LOOT_TABLE,ResourceLocation.parse(definition.table()));
            var table=server.reloadableRegistries().getLootTable(key);
            var params=new LootParams.Builder(player.serverLevel()).withParameter(LootContextParams.ORIGIN,Vec3.atCenterOf(pos)).withOptionalParameter(LootContextParams.THIS_ENTITY,player).withLuck(player.getLuck()).create(LootContextParamSets.CHEST);
            out.addAll(table.getRandomItems(params));out.removeIf(stack->com.ianblk.zianmanager.ManagerEquipment.retired(stack.getItem()));Collections.shuffle(out,random);if(out.size()>definition.rolls())out.subList(definition.rolls(),out.size()).clear();
        }else {var entries=definition.entries().stream().filter(entry->!com.ianblk.zianmanager.ManagerEquipment.retired(item(player,entry.item()).getItem())).toList();for(var entry:definition.independent()?WeightedLoot.independent(entries,random):WeightedLoot.select(entries,definition.rolls(),random)){
            var stack=item(player,entry.item());stack.setCount(entry.min()+random.nextInt(entry.max()-entry.min()+1));
            if(stack.isEmpty() || stack.getCount()>stack.getMaxStackSize())throw new IllegalArgumentException("Cantidad incompatible con el objeto");out.add(stack);
        }
        }
        if(out.isEmpty() && !definition.independent())throw new IllegalArgumentException("La tabla no produjo objetos. Revisa su configuración.");return out;
    }
    public RewardClaim reserve(ServerPlayer player,String key,String preset,int minutes,BlockPos pos) throws Exception{
        boolean repeat=minutes>0 || !requiredKey(key).isEmpty();var old=journal.latest(player.getUUID(),key);if(old!=null && !old.complete()){bindKey(player,old,key);previewKey(player,old);return old;}
        if(old!=null && (!repeat || remaining(player,key,minutes)>0))return old;
        String required=requiredKey(key);if(!required.isEmpty() && !hasKey(player,required))throw new IllegalArgumentException("Necesitas "+net.minecraft.core.registries.BuiltInRegistries.ITEM.get(net.minecraft.resources.ResourceLocation.parse(required)).getDescription().getString()+" para abrir este cofre.");
        List<String> items=roll(player,preset,pos).stream().map(s->s.save(player.registryAccess()).toString()).toList();
        var definition=new RewardDefinition("",0,items,repeat?RewardDefinition.Mode.REPEAT:RewardDefinition.Mode.UNIQUE,minutes);
        journal.reserveAt(player.getUUID(),key,definition,System.currentTimeMillis(),UUID.randomUUID(),store.data().loot().get(preset).independent());var claim=journal.latest(player.getUUID(),key);bindKey(player,claim,key);return claim;
    }
    public void grant(ServerPlayer player,String key,String preset,int minutes,BlockPos pos) throws Exception{
        if(!com.ianblk.zianmanager.ManagerRuntime.get().dungeonTime().allowed(player))return;
        if(!com.ianblk.zianmanager.permission.ManagerPermissions.allows(player.createCommandSourceStack(),"loot",false))return;
        if(minutes>0 && remaining(player,key,minutes)>0){player.sendSystemMessage(Component.literal("Próximo loot en "+((remaining(player,key,minutes)+59999)/60000)+" minuto(s)."));return;}
        var claim=reserve(player,key,preset,minutes,pos);deliver(player,claim.id());
    }
    public List<RewardClaim> pending(UUID uuid){return journal.forPlayer(uuid).stream().filter(c->!c.complete()).toList();}
    public List<RewardClaim> reviewClaims(UUID player){return pending(player).stream().filter(RewardClaim::review).toList();}
    public List<RewardClaim> keyReviews(UUID player){return pending(player).stream().filter(c->{var cost=keys.get(c.id());return cost!=null && (cost.phase().equals("REVIEW") || cost.phase().equals("APPLYING"));}).toList();}
    public List<String> reviewParts(UUID player,UUID id){var claim=journal.get(id);if(claim==null || !claim.player().equals(player))return List.of();var out=new ArrayList<String>();for(int i=0;i<claim.parts().size();i++){var phase=claim.parts().get(i).phase();if(phase==RewardClaim.Phase.APPLYING || phase==RewardClaim.Phase.REVIEW_REQUIRED)out.add(""+(i+1));}return out;}
    public void deliver(ServerPlayer player,UUID id) throws Exception{
        if(!com.ianblk.zianmanager.ManagerRuntime.get().dungeonTime().allowed(player))throw new IllegalArgumentException("Tiempo diario de dungeon agotado");
        if(!com.ianblk.zianmanager.permission.ManagerPermissions.allows(player.createCommandSourceStack(),"loot",false))throw new IllegalArgumentException("No tienes permiso para recibir loot Zian");
        var existing=journal.get(id);if(existing==null || !existing.player().equals(player.getUUID()))throw new IllegalArgumentException("Reclamación inexistente");
        if(existing.complete())return;if(!player.isAlive() || player.isRemoved()){player.sendSystemMessage(Component.literal("Jugador no disponible para recibir loot."));return;}if(existing.review()){player.sendSystemMessage(Component.literal("Entrega en revisión; no se consumirá otra llave."));return;}bindKey(player,existing,existing.trainer());
        var cost=keys.get(id);if(!existing.trainer().startsWith("chest.") && cost!=null && !cost.phase().equals("PAID")){var first=existing.parts().stream().filter(p->p.phase()!=RewardClaim.Phase.DELIVERED).findFirst().orElseThrow();if(!player.isAlive() || !fits(player,item(player,first.data()))){player.sendSystemMessage(Component.literal("Libera espacio; la llave no se ha consumido."));return;}}
        keys.take(id,player.getUUID(),new com.ianblk.zianmanager.core.KeyLedger.Port(){public boolean available(String key){return hasKey(player,key);}public boolean consumeAndSave(String key)throws Exception{var wanted=net.minecraft.core.registries.BuiltInRegistries.ITEM.get(net.minecraft.resources.ResourceLocation.parse(key));for(int i=0;i<player.getInventory().getContainerSize();i++)if(player.getInventory().getItem(i).is(wanted)){player.getInventory().removeItem(i,1);player.getInventory().setChanged();player.inventoryMenu.broadcastChanges();var expected=player.saveWithoutId(new CompoundTag()).get("Inventory");server.getPlayerList().save(player);var file=server.getWorldPath(LevelResource.PLAYER_DATA_DIR).resolve(player.getUUID()+".dat");try(var channel=java.nio.channels.FileChannel.open(file,StandardOpenOption.WRITE)){channel.force(true);}return expected.equals(NbtIo.readCompressed(file,NbtAccounter.create(8L*1024*1024)).get("Inventory"));}return false;}});
        if(existing.trainer().startsWith("chest.")){directChest(player,id);return;}
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
    private void directChest(ServerPlayer player,UUID id)throws Exception{
        var drops=new ArrayList<net.minecraft.world.entity.item.ItemEntity>();var expectedDrops=new ArrayList<ItemStack>();var notices=new ArrayList<DeliveredNotice>();
        BatchDelivery.deliver(journal,player.getUUID(),id,new BatchDelivery.Port(){
            public String unavailable(RewardClaim.Part part){if(!player.isAlive() || player.isRemoved())return "player_unavailable";try{return item(player,part.data()).isEmpty()?"item_unavailable":null;}catch(Exception error){return "invalid_item";}}
            public void apply(RewardClaim.Part part,int index)throws Exception{var stack=item(player,part.data());var original=stack.copy();insertDirect(player,stack);notices.add(new DeliveredNotice(index,original,original.getCount()-stack.getCount(),stack.getCount()));if(!stack.isEmpty()){
                var uuid=UUID.nameUUIDFromBytes(("zianmanager:ground:"+id+":"+index).getBytes(java.nio.charset.StandardCharsets.UTF_8));if(player.serverLevel().getEntity(uuid)!=null)throw new IllegalStateException("Salida de loot ya existente; requiere revisión");
                var drop=new net.minecraft.world.entity.item.ItemEntity(player.serverLevel(),player.getX(),player.getY()+0.5,player.getZ(),stack.copy());drop.setUUID(uuid);drop.setTarget(player.getUUID());drop.setDefaultPickUpDelay();drop.getPersistentData().putString("ZianManagerClaim",id.toString());
                if(!player.serverLevel().addFreshEntity(drop))throw new IllegalStateException("Otro mod rechazó el objeto en el suelo");drops.add(drop);expectedDrops.add(stack.copy());
            }}
            public boolean commit()throws Exception{player.getInventory().setChanged();player.inventoryMenu.broadcastChanges();var expected=player.saveWithoutId(new CompoundTag()).get("Inventory");server.getPlayerList().save(player);var file=server.getWorldPath(LevelResource.PLAYER_DATA_DIR).resolve(player.getUUID()+".dat");try(var channel=java.nio.channels.FileChannel.open(file,StandardOpenOption.WRITE)){channel.force(true);}if(!drops.isEmpty())player.serverLevel().save(null,true,false);var saved=NbtIo.readCompressed(file,NbtAccounter.create(8L*1024*1024));if(!expected.equals(saved.get("Inventory")))return false;for(int i=0;i<drops.size();i++)if(drops.get(i).isRemoved() || player.serverLevel().getEntity(drops.get(i).getUUID())!=drops.get(i) || !ItemStack.matches(drops.get(i).getItem(),expectedDrops.get(i)))return false;return true;}
        });
        var claim=journal.get(id);var summary=Component.literal("[Cofre] Recibiste: ").withStyle(net.minecraft.ChatFormatting.GOLD);boolean announced=false;
        for(var notice:notices)if(claim.parts().get(notice.index()).phase()==RewardClaim.Phase.DELIVERED){if(notice.inventory()>0){if(announced)summary.append(Component.literal(", ").withStyle(net.minecraft.ChatFormatting.GRAY));summary.append(noticePart(notice.item(),notice.inventory(),"inventario",net.minecraft.ChatFormatting.GREEN));announced=true;}if(notice.ground()>0){if(announced)summary.append(Component.literal(", ").withStyle(net.minecraft.ChatFormatting.GRAY));summary.append(noticePart(notice.item(),notice.ground(),"suelo",net.minecraft.ChatFormatting.YELLOW));announced=true;}}
        if(announced)player.sendSystemMessage(summary.append(Component.literal(".").withStyle(net.minecraft.ChatFormatting.GRAY)));
        player.displayClientMessage(Component.literal(claim.complete()?(drops.isEmpty()?"Loot recibido.":"Loot recibido; el sobrante está en el suelo."):claim.review()?"Entrega en revisión; no se repetirá automáticamente.":"Loot pendiente."),true);
    }
    private record DeliveredNotice(int index,ItemStack item,int inventory,int ground){}
    private static Component noticePart(ItemStack stack,int count,String location,net.minecraft.ChatFormatting color){return Component.literal(count+" × ").withStyle(color).append(stack.getHoverName().copy().withStyle(color)).append(Component.literal(" ("+location+")").withStyle(net.minecraft.ChatFormatting.GRAY));}
    private static void insertDirect(ServerPlayer player,ItemStack stack){
        var inventory=player.getInventory();for(int i=0;i<36 && !stack.isEmpty();i++){var slot=inventory.getItem(i);if(!slot.isEmpty() && ItemStack.isSameItemSameComponents(slot,stack)){int amount=Math.min(stack.getCount(),Math.max(0,Math.min(slot.getMaxStackSize(),inventory.getMaxStackSize())-slot.getCount()));slot.grow(amount);stack.shrink(amount);}}
        for(int i=0;i<36 && !stack.isEmpty();i++)if(inventory.getItem(i).isEmpty()){int amount=Math.min(stack.getCount(),Math.min(stack.getMaxStackSize(),inventory.getMaxStackSize()));inventory.setItem(i,stack.copyWithCount(amount));stack.shrink(amount);}
    }
    private String requiredKey(String key){if(!key.startsWith("chest."))return "";try{var chest=store.data().chests().get(UUID.fromString(key.substring(6)));return chest==null?"":com.ianblk.zianmanager.ManagerBlocks.keyFor(chest.block());}catch(IllegalArgumentException error){return "";}}
    private void bindKey(ServerPlayer player,RewardClaim claim,String key)throws Exception{if(key.startsWith("chest."))keys.bind(claim.id(),player.getUUID(),requiredKey(key));}
    private static boolean hasKey(ServerPlayer player,String key){var wanted=net.minecraft.core.registries.BuiltInRegistries.ITEM.get(net.minecraft.resources.ResourceLocation.parse(key));for(int i=0;i<player.getInventory().getContainerSize();i++)if(player.getInventory().getItem(i).is(wanted))return true;return false;}
    private void previewKey(ServerPlayer player,RewardClaim claim){var cost=keys.get(claim.id());if(cost!=null && !cost.key().isEmpty() && cost.phase().equals("RESERVED") && !hasKey(player,cost.key()))throw new IllegalArgumentException("Necesitas la llave correspondiente; tu loot pendiente está conservado.");}
    public String keyNotice(UUID id){var c=keys.get(id);return c==null || c.key().isBlank()?"":c.phase().equals("PAID")?" · Llave ya consumida":" · Se consume 1 llave al recibir";}
    public void resolveKey(UUID claim,UUID player,UUID admin,String evidence)throws Exception{if(evidence==null || evidence.length()<8 || evidence.length()>400)throw new IllegalArgumentException("Escribe evidencia de 8–400 caracteres");keys.resolve(claim,player,admin+": "+evidence);com.ianblk.zianmanager.ZianManager.LOGGER.info("[ZIAN-MANAGER] action=key_review_resolve admin={} player={} claim={}",admin,player,claim);}
    public void resolve(UUID player,UUID id,int part,UUID admin,String evidence) throws Exception{journal.resolve(player,id,part,admin,"confirmed_compensated",evidence);}
    public static ItemStack item(ServerPlayer player,String data){try{return ItemStack.parseOptional(player.registryAccess(),TagParser.parseTag(data));}catch(Exception e){throw new IllegalArgumentException("Objeto ilegible",e);}}
    private static boolean fits(ServerPlayer player,ItemStack stack){int space=0;for(int i=0;i<36;i++){var slot=player.getInventory().getItem(i);space+=slot.isEmpty()?stack.getMaxStackSize():ItemStack.isSameItemSameComponents(slot,stack)?Math.max(0,slot.getMaxStackSize()-slot.getCount()):0;if(space>=stack.getCount())return true;}return false;}
}
