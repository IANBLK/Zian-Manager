package com.ianblk.zianmanager.core;
import com.ianblk.zianmanager.core.Definitions.*;
import com.ianblk.zianmanager.core.EncounterLedger.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.Path;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
class ZoneRefreshTest {
 @TempDir Path dir;
 @Test void legacyZonesKeepReferenceAsNameAndCustomNameSurvivesSave()throws Exception{var store=new ManagerStore(dir.resolve("store.json"));var old=new ZoneSpec("crypt","minecraft:overworld",new Point(0,64,0,0),new Point(6,70,6,0),List.of(),List.of("guard"),1,5,600,"",true);assertEquals("crypt",old.name());var named=new ZoneSpec(old.id(),old.dimension(),old.first(),old.second(),old.points(),old.team(),1,5,600,"",true,null,0,0,0,0,"Cripta de las Sombras");store.put(named);assertEquals(named.name(),new ManagerStore(dir.resolve("store.json")).data().zones().get("crypt").name());}
 @Test void editingCooldownKeepsCompletionTimeAndAppliesImmediately()throws Exception{var j=new EncounterLedger(dir.resolve("runs.json"));var run=new Run(UUID.randomUUID(),"crypt",1,Phase.COMPLETE,700000,List.of(),Set.of(),2,1);j.put(run);j.updateWait("crypt",5,5,600,60);assertEquals(160000,j.get("crypt").readyAt());j.updateWait("crypt",5,5,60,120);assertEquals(220000,new EncounterLedger(dir.resolve("runs.json")).get("crypt").readyAt());assertEquals(run.uuid(),j.get("crypt").uuid());}
 @Test void pauseEditsRebaseWaitingWaveButDoNotRestartActiveWave()throws Exception{var j=new EncounterLedger(dir.resolve("runs.json"));var run=new Run(UUID.randomUUID(),"crypt",1,Phase.WAITING,15000,List.of(),Set.of(),2,1);j.put(run);j.updateWait("crypt",5,10,600,60);assertEquals(20000,j.get("crypt").readyAt());var active=new Run(run.uuid(),"crypt",1,Phase.ACTIVE,0,List.of(),Set.of(),2,1);j.put(active);j.updateWait("crypt",10,20,60,120);assertEquals(active,j.get("crypt"));}
}
