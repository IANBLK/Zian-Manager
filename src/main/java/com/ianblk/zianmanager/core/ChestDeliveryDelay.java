package com.ianblk.zianmanager.core;
import java.util.*;
/** An animation gate; the authoritative reward itself remains in the durable journal. */
public final class ChestDeliveryDelay {
 public static final int TICKS=30;
 public record Entry(UUID player,UUID claim,long due){}
 private final Map<UUID,Entry> entries=new HashMap<>();
 public boolean schedule(UUID player,UUID claim,long now){var previous=entries.get(claim);if(previous!=null){if(!previous.player().equals(player))throw new IllegalArgumentException("Claim owner mismatch");return false;}entries.put(claim,new Entry(player,claim,now+TICKS));return true;}
 public boolean blocked(UUID player,UUID claim,long now){var e=entries.get(claim);return e!=null&&e.player().equals(player)&&now<e.due();}
 public List<Entry> ready(long now){var ready=new ArrayList<Entry>();var it=entries.values().iterator();while(it.hasNext()){var e=it.next();if(now>=e.due()){ready.add(e);it.remove();}}return ready;}
}
