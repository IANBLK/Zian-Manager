package com.ianblk.zianmanager.core;
import com.ianblk.zianmanager.core.Definitions.*;
import com.ianblk.zianmanager.reward.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.Path;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
class IndependentLootTest {
 @TempDir Path dir;
 private static final List<LootEntry> KEYS=List.of(new LootEntry("common",10,1,1),new LootEntry("rare",5,1,1));
 @Test void keyPercentagesAreAbsoluteAndEmptyDropsArePossible(){var random=new Random(882);int common=0,rare=0,none=0,both=0;for(int i=0;i<100000;i++){var chosen=WeightedLoot.independent(KEYS,random);if(chosen.contains(KEYS.get(0)))common++;if(chosen.contains(KEYS.get(1)))rare++;if(chosen.isEmpty())none++;if(chosen.size()==2)both++;}assertTrue(common>9500 && common<10500);assertTrue(rare>4500 && rare<5500);assertTrue(none>84500 && none<86500);assertTrue(both>350 && both<650);}
 @Test void bossesAndChestsCannotUseEmptyDropMode(){for(String category:List.of("BOSS","CHEST"))assertThrows(IllegalArgumentException.class,()->new LootSpec("keys",category,2,KEYS,"",true));assertDoesNotThrow(()->new LootSpec("keys","MOB",2,KEYS,"",true));assertEquals(2,WeightedLoot.select(KEYS,2,new Random(1)).size());}
 @Test void chanceTableCannotBeAssignedToChestOrBossAndCannotConvertSharedTable()throws Exception{var store=new ManagerStore(dir.resolve("manager.json"));var chance=new LootSpec("keys","MOB",2,KEYS,"",true);store.put(chance);assertThrows(IllegalArgumentException.class,()->store.put(new ChestSpec(UUID.randomUUID(),"minecraft:overworld",0,64,0,"keys",1)));assertThrows(IllegalArgumentException.class,()->store.put(new MobSpec("boss","Boss","minecraft:zombie",40,6,0,0,0,List.of(),Map.of(),"keys",true)));store.put(new LootSpec("shared","MOB",2,KEYS,""));store.put(new ChestSpec(UUID.randomUUID(),"minecraft:overworld",0,64,0,"shared",1));assertThrows(IllegalArgumentException.class,()->store.put(new LootSpec("shared","MOB",2,KEYS,"",true)));assertFalse(store.data().loot().get("shared").independent());}
 @Test void noDropOutcomeIsPersistedAndCannotRerollSameMobAfterRestart()throws Exception{var path=dir.resolve("claims.json");var j=RewardJournal.open(path);var player=UUID.randomUUID();var empty=new RewardDefinition("",0,List.of());assertTrue(j.reserveAt(player,"mob.unique",empty,1000,UUID.randomUUID(),true));assertTrue(j.latest(player,"mob.unique").complete());j=RewardJournal.open(path);assertFalse(j.reserveAt(player,"mob.unique",new RewardDefinition("",0,List.of("key")),2000,UUID.randomUUID()));assertTrue(j.latest(player,"mob.unique").parts().isEmpty());}
}
