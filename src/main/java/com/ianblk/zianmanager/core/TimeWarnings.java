package com.ianblk.zianmanager.core;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.*;

/** Persisted daily warning thresholds; rising balances rearm only the thresholds passed. */
public final class TimeWarnings {
 private static final long[] THRESHOLDS={300,60,30};
 private record Row(String day,int mask){Row{LocalDate.parse(day);if(mask<0 || mask>7)throw new IllegalArgumentException("Invalid time warning mask");}}
 private record Data(int schema,Map<UUID,Row> players){Data{if(schema!=1 || players.size()>100000)throw new IllegalArgumentException("Invalid time warnings");players=Map.copyOf(players);}}
 private final Path file;private Map<UUID,Row> players;
 public TimeWarnings(Path file)throws IOException{this.file=file;players=AtomicJson.read(file,Data.class,new Data(1,Map.of())).players();}
 public OptionalLong poll(UUID player,LocalDate day,long remaining)throws IOException{
  if(remaining<=0)return OptionalLong.empty();
  var old=players.get(player);int mask=old!=null && old.day().equals(day.toString())?old.mask():0;
  int reached=0;for(int i=0;i<THRESHOLDS.length;i++)if(remaining<=THRESHOLDS[i])reached|=1<<i;
  boolean notify=(reached & ~mask)!=0;var row=new Row(day.toString(),reached);
  if((old!=null || reached!=0) && !row.equals(old)){
   var next=new HashMap<>(players);next.put(player,row);AtomicJson.write(file,new Data(1,next));players=Map.copyOf(next);
  }
  return notify?OptionalLong.of(remaining):OptionalLong.empty();
 }
}
