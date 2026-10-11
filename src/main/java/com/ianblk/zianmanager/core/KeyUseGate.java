package com.ianblk.zianmanager.core;
import java.util.*;
/** One gate per player across all keyed crates, including rejected/review attempts. */
public final class KeyUseGate {
 private final Map<UUID,Long> until=new HashMap<>();
 public long enter(UUID player,long tick){long remaining=until.getOrDefault(player,0L)-tick;if(remaining>0)return remaining;until.put(player,tick+40);return 0;}
 public void clear(){until.clear();}
 public void expire(long tick){until.values().removeIf(end->end<=tick);}
}
