package com.ianblk.zianmanager;
import com.ianblk.zianmanager.core.*;
import com.ianblk.zianmanager.core.Definitions.*;
import com.ianblk.zianmanager.core.EncounterLedger.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.*;
import net.minecraft.nbt.CompoundTag;
import java.util.*;
/** Temporary vanilla text displays: fixed world center, public countdown, no saved duplicates. */
public final class ZoneHolograms {
 private final Map<String,Display.TextDisplay> displays=new HashMap<>();
 public boolean owns(UUID uuid){return displays.values().stream().anyMatch(e->e.getUUID().equals(uuid));}
 public void clear(){displays.values().forEach(Entity::discard);displays.clear();}
 public void update(MinecraftServer server,ManagerStore store,EncounterLedger ledger,long now){
  var active=new HashSet<String>();
  for(var zone:store.data().zones().values()){var run=ledger.get(zone.id());if(!zone.enabled() || run==null || run.phase()!=Phase.COMPLETE || run.readyAt()<=now)continue;
   var level=server.getLevel(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.DIMENSION,net.minecraft.resources.ResourceLocation.parse(zone.dimension())));if(level==null)continue;
   double x=zone.center()!=null?zone.center().x()+0.5:(zone.first().x()+zone.second().x()+1)/2,z=zone.center()!=null?zone.center().z()+0.5:(zone.first().z()+zone.second().z()+1)/2,y=(zone.center()!=null?zone.center().y():Math.min(zone.first().y(),zone.second().y()))+3;
   if(!level.getChunkSource().hasChunk(BlockPos.containing(x,y,z).getX()>>4,BlockPos.containing(x,y,z).getZ()>>4))continue;active.add(zone.id());
   var display=displays.get(zone.id());boolean fresh=display==null || display.isRemoved() || display.level()!=level;if(fresh){if(display!=null)display.discard();display=EntityType.TEXT_DISPLAY.create(level);displays.put(zone.id(),display);}
   String text="Dungeon · "+zone.id()+"\nLa zona se regenerará en "+countdown(run.readyAt()-now);
   var tag=new CompoundTag();tag.putString("text",AtomicJson.GSON.toJson(Map.of("text",text,"color","gold")));tag.putString("billboard","center");tag.putInt("line_width",240);tag.putBoolean("Invulnerable",true);tag.putBoolean("NoGravity",true);display.load(tag);display.moveTo(x,y,z,0,0);display.getPersistentData().putBoolean("ZianManagerCountdown",true);if(fresh && !level.addFreshEntity(display)){displays.remove(zone.id());display.discard();}
  }
  displays.entrySet().removeIf(e->{if(active.contains(e.getKey()))return false;e.getValue().discard();return true;});
 }
 public static String countdown(long millis){long seconds=Math.max(0,(millis+999)/1000);return seconds>=60?seconds/60+" min "+seconds%60+" s":seconds+" s";}
}
