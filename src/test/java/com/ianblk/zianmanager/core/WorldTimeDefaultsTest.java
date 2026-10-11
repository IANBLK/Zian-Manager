package com.ianblk.zianmanager.core;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.Path;
import java.time.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
class WorldTimeDefaultsTest {
 @TempDir Path dir;
 @Test void farmingBudgetsAreThreeAndFourHoursOnWeekdaysAndWeekends(){for(String id:List.of("nether","end","farmeo")){var s=WorldTimeDefaults.farming(id);for(String date:List.of("2026-10-09T12:00:00Z","2026-10-10T12:00:00Z")){assertEquals(10800,s.limit(Instant.parse(date),false));assertEquals(14400,s.limit(Instant.parse(date),true));}assertFalse(s.enabled());assertEquals("zianmanager.farming.rank",s.vipPermission());}}
 @Test void worldsAndNamesAreDistinct(){assertEquals(List.of("minecraft:the_nether"),WorldTimeDefaults.farming("nether").worlds());assertEquals(List.of("minecraft:the_end"),WorldTimeDefaults.farming("end").worlds());assertEquals(List.of("farmeo"),WorldTimeDefaults.farming("farmeo").worlds());assertThrows(IllegalArgumentException.class,()->WorldTimeDefaults.farming("spawn"));}
 @Test void independentConsumptionBonusesAndRestartDoNotChangeDungeon()throws Exception{
  var id=UUID.randomUUID();var day=LocalDate.of(2026,10,10);var all=new LinkedHashMap<String,DailyDungeonTime>();for(String key:List.of("dungeon","nether","end","farmeo"))all.put(key,new DailyDungeonTime(dir.resolve(key+".json")));
  all.get("nether").charge(Map.of(id,10800L),day);all.get("end").charge(Map.of(id,600L),day);all.get("farmeo").grant(id,day,1800);
  assertEquals(10800,new DailyDungeonTime(dir.resolve("nether.json")).used(id,day));assertEquals(600,new DailyDungeonTime(dir.resolve("end.json")).used(id,day));assertEquals(0,all.get("farmeo").used(id,day));assertEquals(1800,all.get("farmeo").bonus(id,day));assertEquals(0,all.get("dungeon").used(id,day));assertEquals(0,all.get("end").bonus(id,day));
  for(var time:all.values()){assertEquals(0,time.used(id,day.plusDays(1)));assertEquals(0,time.bonus(id,day.plusDays(1)));}
 }
}
