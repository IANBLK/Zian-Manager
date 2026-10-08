package com.ianblk.zianmanager.core;
import com.ianblk.zianmanager.core.Definitions.*;
import com.ianblk.zianmanager.core.EncounterLedger.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
class ManagerCoreTest {
    @TempDir Path dir;
    private static List<LootEntry> entries(int count){var result=new ArrayList<LootEntry>();for(int i=0;i<count;i++)result.add(new LootEntry("item"+i,i+1,1,1));return result;}
    @Test void mobSelectsTwoOfTenWithoutDuplicates(){var out=WeightedLoot.select(entries(10),2,new Random(1));assertEquals(2,out.size());assertEquals(2,new HashSet<>(out).size());}
    @Test void bossSelectsFiveOfTenWithoutDuplicates(){var out=WeightedLoot.select(entries(10),5,new Random(2));assertEquals(5,out.size());assertEquals(5,new HashSet<>(out).size());}
    @Test void chestSelectsTenOfTwentyWithoutDuplicates(){var out=WeightedLoot.select(entries(20),10,new Random(3));assertEquals(10,out.size());assertEquals(10,new HashSet<>(out).size());}
    @Test void selectionIsBoundedByAvailableEntries(){assertEquals(3,WeightedLoot.select(entries(3),10,new Random(4)).size());}
    @Test void weightedChoiceRespectsRelativeLikelihood(){var list=List.of(new LootEntry("common",99,1,1),new LootEntry("rare",1,1,1));var random=new Random(5);long rare=0;for(int i=0;i<10000;i++)if(WeightedLoot.select(list,1,random).getFirst().item().equals("rare"))rare++;assertTrue(rare>50 && rare<170);}
    @Test void invalidWeightsAndQuantitiesRejected(){assertThrows(IllegalArgumentException.class,()->new LootEntry("x",0,1,1));assertThrows(IllegalArgumentException.class,()->new LootEntry("x",1,2,1));}
    @Test void definitionsSurviveRestart(){var point=new Point(0,70,0,0);assertDoesNotThrow(()->{var store=new ManagerStore(dir.resolve("defs.json"));store.put(new LootSpec("treasure","CHEST",10,entries(20),""));var read=new ManagerStore(dir.resolve("defs.json"));assertEquals(20,read.data().loot().get("treasure").entries().size());});}
    @Test void corruptedStoreIsNotReset(){Path path=dir.resolve("defs.json");assertDoesNotThrow(()->Files.writeString(path,"invalid"));assertThrows(java.io.IOException.class,()->new ManagerStore(path));assertDoesNotThrow(()->assertEquals("invalid",Files.readString(path)));}
    @Test void enabledZonesRequireThreeToSixMobsAndDistinctSpawnSlots(){var p=new Point(0,70,0,0);assertThrows(IllegalArgumentException.class,()->new ZoneSpec("room","minecraft:overworld",p,p,List.of(p),List.of("mob"),1,5,60,"",true));assertThrows(IllegalArgumentException.class,()->new ZoneSpec("room","minecraft:overworld",p,new Point(10,75,10,0),List.of(p,p,p),List.of("mob","mob","mob"),1,5,60,"",true));}
    @Test void cuboidIncludesBothSelectedBlocks(){var zone=new ZoneSpec("room","minecraft:overworld",new Point(0,70,0,0),new Point(5,75,5,0),List.of(),List.of(),1,5,60,"",false);assertTrue(zone.contains("minecraft:overworld",5.9,75,5.9));assertFalse(zone.contains("minecraft:the_nether",1,71,1));}
    @Test void deathReplayCannotCompleteAnotherWave() throws Exception{
        var ledger=new EncounterLedger(dir.resolve("encounters.json"));UUID run=UUID.randomUUID(),mob=UUID.randomUUID();var slot=new Spawn(mob,"guard",new Point(0,70,0,0),false);ledger.put(new Run(run,"room",1,Phase.ACTIVE,0,List.of(slot),Set.of(UUID.randomUUID())));
        assertTrue(ledger.defeated("room",run,mob,2,5,60,1000));assertEquals(Phase.WAITING,ledger.get("room").phase());assertEquals(6000,ledger.get("room").readyAt());assertFalse(ledger.defeated("room",run,mob,2,5,60,1000));assertEquals(Phase.WAITING,new EncounterLedger(dir.resolve("encounters.json")).get("room").phase());
    }
    @Test void completedEncounterCooldownSurvivesRestart() throws Exception{
        var ledger=new EncounterLedger(dir.resolve("encounters.json"));UUID run=UUID.randomUUID(),mob=UUID.randomUUID();ledger.put(new Run(run,"room",1,Phase.ACTIVE,0,List.of(new Spawn(mob,"guard",new Point(0,70,0,0),false)),Set.of()));ledger.defeated("room",run,mob,1,5,60,1000);var read=new EncounterLedger(dir.resolve("encounters.json"));assertEquals(Phase.COMPLETE,read.get("room").phase());assertEquals(61000,read.get("room").readyAt());
    }
    @Test void unknownSchemaIsPreserved() throws Exception{Path path=dir.resolve("encounters.json");Files.writeString(path,"{\"schema\":7,\"runs\":{}}");assertThrows(Exception.class,()->new EncounterLedger(path));assertTrue(Files.readString(path).contains("7"));}
}
