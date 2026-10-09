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
 private static final class Label extends Display.TextDisplay {Label(net.minecraft.world.level.Level level){super(EntityType.TEXT_DISPLAY,level);}void text(CompoundTag tag){super.readAdditionalSaveData(tag);}}
 private final Map<String,Label> displays=new HashMap<>();
 public boolean owns(UUID uuid){return displays.values().stream().anyMatch(e->e.getUUID().equals(uuid));}
 public void clear(){displays.values().forEach(Entity::discard);displays.clear();}
 public void update(MinecraftServer server,ManagerStore store,EncounterLedger ledger,long now){
  var active=new HashSet<String>();
  for(var zone:store.data().zones().values()){var run=ledger.get(zone.id());if(zone.first()==null || zone.second()==null)continue;
   var level=server.getLevel(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.DIMENSION,net.minecraft.resources.ResourceLocation.parse(zone.dimension())));if(level==null)continue;
   double x=zone.center()!=null?zone.center().x()+0.5:(zone.first().x()+zone.second().x()+1)/2,z=zone.center()!=null?zone.center().z()+0.5:(zone.first().z()+zone.second().z()+1)/2,y=(zone.center()!=null?zone.center().y():Math.min(zone.first().y(),zone.second().y()))+3;
   if(!level.getChunkSource().hasChunk(BlockPos.containing(x,y,z).getX()>>4,BlockPos.containing(x,y,z).getZ()>>4))continue;active.add(zone.id());
   var display=displays.get(zone.id());boolean fresh=display==null || display.isRemoved() || display.level()!=level;if(fresh){if(display!=null)display.discard();display=new Label(level);display.setInvulnerable(true);display.setNoGravity(true);displays.put(zone.id(),display);}
   String status=!zone.enabled()?"Dungeon desactivada":run==null || run.phase()==Phase.CANCELLED || run.phase()==Phase.COMPLETE && run.readyAt()<=now?"Disponible · entra para comenzar":run.phase()==Phase.COMPLETE?"La zona se regenerará en "+countdown(run.readyAt()-now):run.phase()==Phase.WAITING?"Próxima oleada en "+countdown(run.readyAt()-now):run.phase()==Phase.REVIEW?"Zona en revisión · avisa al administrador":"En curso · oleada "+run.wave()+" / "+zone.waves();String text=zone.name()+"\n"+status;
   var tag=new CompoundTag();tag.putString("text",AtomicJson.GSON.toJson(Map.of("text",text,"color","gold")));tag.putString("billboard","center");tag.putInt("line_width",240);tag.putString("alignment","center");tag.putFloat("view_range",4);var brightness=new CompoundTag();brightness.putInt("block",15);brightness.putInt("sky",15);tag.put("brightness",brightness);tag.putBoolean("shadow",true);tag.putBoolean("see_through",true);display.text(tag);display.moveTo(x,y,z,0,0);display.getPersistentData().putBoolean("ZianManagerCountdown",true);if(fresh && !level.addFreshEntity(display)){displays.remove(zone.id());display.discard();}
  }
  displays.entrySet().removeIf(e->{if(active.contains(e.getKey()))return false;e.getValue().discard();return true;});
 }
 public static String countdown(long millis){long seconds=Math.max(0,(millis+999)/1000);return seconds>=60?seconds/60+" min "+seconds%60+" s":seconds+" s";}
}
