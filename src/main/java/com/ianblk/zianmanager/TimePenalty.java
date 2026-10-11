package com.ianblk.zianmanager;

import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.*;

/** Infinite penalties are tagged per player, with prior effects kept in vanilla's hidden chain. */
final class TimePenalty {
 private static final String KEY="ZianManagerTimePenalty";
 private static final List<Holder<MobEffect>> EFFECTS=List.of(MobEffects.MOVEMENT_SLOWDOWN,MobEffects.DARKNESS,MobEffects.WEAKNESS);
 private static String name(Holder<MobEffect> effect){return effect.getRegisteredName();}
 static void apply(ServerPlayer player){
  var owned=player.getPersistentData().getCompound(KEY);
  for(var effect:EFFECTS){
   var current=player.getEffect(effect);
   if(current!=null && (current.getAmplifier()>4 || current.getAmplifier()==4 && current.isInfiniteDuration()))continue;
   // Preserve even an existing level-V finite effect as a hidden, naturally aging effect.
   if(current!=null && !player.removeEffect(effect))continue;
   if(player.addEffect(new MobEffectInstance(effect,MobEffectInstance.INFINITE_DURATION,4,false,false,true,current)))owned.putBoolean(name(effect),true);
   else if(current!=null)player.addEffect(current);
  }
  if(!owned.isEmpty())player.getPersistentData().put(KEY,owned);
 }
 static void clear(ServerPlayer player){
  var owned=player.getPersistentData().getCompound(KEY);if(owned.isEmpty())return;
  for(var effect:EFFECTS){
   String id=name(effect);if(!owned.getBoolean(id))continue;var current=player.getEffect(effect);
   if(current==null){owned.remove(id);continue;}
   var saved=(CompoundTag)current.save();var clean=strip(saved);
   if(clean!=null && clean.equals(saved)){owned.remove(id);continue;}
   MobEffectInstance restored=null;
   if(clean!=null){clean.put("id",saved.get("id").copy());restored=MobEffectInstance.load(clean);}
   if(!player.removeEffect(effect))continue;
   if(restored!=null && (restored.isInfiniteDuration() || restored.getDuration()>0))player.addEffect(restored);
   owned.remove(id);
  }
  if(owned.isEmpty())player.getPersistentData().remove(KEY);else player.getPersistentData().put(KEY,owned);
 }
 private static CompoundTag strip(CompoundTag node){
  var hidden=node.contains("hidden_effect",10)?strip(node.getCompound("hidden_effect")):null;
  if(node.getInt("duration")==0)return hidden;
  if(node.getInt("amplifier")==4 && node.getInt("duration")==MobEffectInstance.INFINITE_DURATION)return hidden;
  var clean=node.copy();if(hidden==null)clean.remove("hidden_effect");else clean.put("hidden_effect",hidden);return clean;
 }
}
