package lat.rassvet.rules;

import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.util.SideEffect;
import com.sk89q.worldedit.util.SideEffectSet;
import org.bukkit.block.data.type.Leaves;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.world.block.BaseBlock;
import org.bukkit.*;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import java.io.*;
import java.util.*;

/** Fixed, administrator-only recovery of the supplied lobby; never edits the main world. */
final class LobbyPasteService {
 private final JavaPlugin plugin;private volatile boolean loading;private volatile long generation;private BukkitRunnable task;
 private record Placement(BlockVector3 position,BaseBlock block){}
 LobbyPasteService(JavaPlugin plugin){this.plugin=plugin;}
 void start(CommandSender sender){
  if(loading || task!=null){sender.sendMessage("Ya hay un pegado de lobby en curso.");return;}
  World target=Bukkit.getWorld("gyms");if(target==null || !target.getName().equals("gyms")){sender.sendMessage("El mundo gyms no estÃ¡ cargado.");return;}
  File source=new File(plugin.getDataFolder().getParentFile(),"WorldEdit/schematics/freemap18.schem");
  if(!source.isFile() || source.length()>16*1024*1024){sender.sendMessage("No estÃ¡ disponible freemap18.schem.");return;}
  long ticket=++generation;loading=true;sender.sendMessage("Preparando el lobby para Gyms por tandas, sin copiar aire.");
  Bukkit.getScheduler().runTaskAsynchronously(plugin,()->{
   try{
    var format=ClipboardFormats.findByFile(source);if(format==null)throw new IOException("Formato de esquema desconocido");
    Clipboard clip;try(var input=new FileInputStream(source);var reader=format.getReader(input)){clip=reader.read();}
    if(clip.getRegion().getVolume()>20000000L)throw new IOException("Esquema demasiado grande");
    var placements=new ArrayList<Placement>();var anchor=BlockVector3.at(0,64,0);var origin=clip.getOrigin();
    for(var point:clip.getRegion()){
     var state=clip.getBlock(point);String id=state.getBlockType().getId();if(id.equals("minecraft:air") || id.equals("minecraft:cave_air") || id.equals("minecraft:void_air"))continue;
     var destination=point.subtract(origin).add(anchor);
     if(destination.y()<target.getMinHeight() || destination.y()>=target.getMaxHeight())throw new IOException("Bloque fuera de la altura del mundo: "+destination);
     if(Math.abs(destination.x())>512 || Math.abs(destination.z())>512)throw new IOException("El esquema excede el Ã¡rea de Gyms permitida");
     BaseBlock full=clip.getFullBlock(point);
     var data=BukkitAdapter.adapt(state);
     if(data instanceof Leaves leaves){leaves.setPersistent(true);full=BukkitAdapter.adapt(leaves).toBaseBlock();}
     placements.add(new Placement(destination,full));
     if(placements.size()>1200000)throw new IOException("Demasiados bloques sÃ³lidos");
    }
    if(placements.size()!=911569)throw new IOException("La lectura del esquema no conserva todos los bloques: "+placements.size()+" de 911569");
    placements.sort(Comparator.comparingInt(p->p.position().y()));
    if(!plugin.isEnabled() || ticket!=generation)return;
    Bukkit.getScheduler().runTask(plugin,()->begin(sender,target,placements,ticket));
   }catch(Exception|LinkageError error){if(ticket==generation)if(ticket==generation)loading=false;plugin.getLogger().log(java.util.logging.Level.SEVERE,"No se pudo preparar el lobby",error);}
  });
 }
 private void begin(CommandSender sender,World target,List<Placement> blocks,long ticket){
  if(ticket!=generation)return;loading=false;if(!plugin.isEnabled() || Bukkit.getWorld("gyms")!=target)return;
  // Remove only the temporary bedrock entry platform created for this new world.
  for(int x=-2;x<=2;x++)for(int z=-2;z<=2;z++){var b=target.getBlockAt(x,64,z);if(b.getType()==Material.BEDROCK)b.setType(Material.AIR,false);}
  plugin.getLogger().info("Gyms lobby prepared: "+blocks.size()+" non-air blocks; target=gyms origin=0,64,0");
  task=new BukkitRunnable(){int cursor,nextNotice;
   @Override public void run(){
    if(Bukkit.getWorld("gyms")!=target){cancel();task=null;return;}
    long started=System.nanoTime();int count=0;
    try(var edit=WorldEdit.getInstance().newEditSessionBuilder().world(BukkitAdapter.adapt(target)).maxBlocks(2000).build()){
     edit.setSideEffectApplier(SideEffectSet.defaults().with(SideEffect.NEIGHBORS,SideEffect.State.OFF).with(SideEffect.VALIDATION,SideEffect.State.OFF).with(SideEffect.UPDATE,SideEffect.State.OFF));
     edit.setSideEffectApplier(SideEffectSet.defaults().with(SideEffect.NEIGHBORS,SideEffect.State.OFF).with(SideEffect.VALIDATION,SideEffect.State.OFF).with(SideEffect.UPDATE,SideEffect.State.OFF));edit.setReorderMode(EditSession.ReorderMode.NONE);edit.disableBuffering();
     while(cursor<blocks.size() && count<1000 && System.nanoTime()-started<6000000L){var p=blocks.get(cursor);edit.setBlock(p.position(),p.block());cursor++;count++;}
    }catch(Exception|LinkageError error){cancel();task=null;plugin.getLogger().log(java.util.logging.Level.SEVERE,"Lobby paste stopped at block "+cursor,error);return;}
    int percent=blocks.isEmpty()?100:cursor*100/blocks.size();if(percent>=nextNotice){plugin.getLogger().info("Gyms lobby progress: "+percent+"% ("+cursor+"/"+blocks.size()+")");nextNotice=percent+10;}
    if(cursor==blocks.size()){cancel();task=null;target.setSpawnLocation(0,64,0,0);target.save();plugin.getLogger().info("Gyms lobby completed: "+cursor+" non-air blocks, saved; no entities imported");sender.sendMessage("Lobby de Gyms terminado y guardado.");}
   }
  };task.runTaskTimer(plugin,1,1);
 }
 void cancel(){generation++;loading=false;if(task!=null){task.cancel();task=null;}}
}
