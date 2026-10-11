package com.ianblk.zianmanager.reward;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.Path;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
class BatchDeliveryTest {
 @TempDir Path dir;
 private static class Port implements BatchDelivery.Port {int inventory,ground,commits;boolean confirmed=true;int fail=-1;public String unavailable(RewardClaim.Part part){return null;}public void apply(RewardClaim.Part part,int index)throws Exception{if(index==fail)throw new java.io.IOException("cancelled");if(inventory<1)inventory++;else ground++;}public boolean commit(){commits++;return confirmed;}}
 private UUID reserve(RewardJournal j,UUID p)throws Exception{j.reserveAt(p,"chest.test",new RewardDefinition("",0,List.of("a","b","c"),RewardDefinition.Mode.REPEAT,10),System.currentTimeMillis(),UUID.randomUUID());return j.latest(p,"chest.test").id();}
 @Test void inventoryAndOverflowCommitOnceWithoutReplay()throws Exception{var path=dir.resolve("claims.json");var j=RewardJournal.open(path);var p=UUID.randomUUID();var id=reserve(j,p);var port=new Port();BatchDelivery.deliver(j,p,id,port);assertEquals(1,port.inventory);assertEquals(2,port.ground);assertEquals(1,port.commits);assertTrue(j.get(id).complete());BatchDelivery.deliver(RewardJournal.open(path),p,id,port);assertEquals(2,port.ground);assertEquals(1,port.commits);}
 @Test void failedWorldCommitRequiresReviewAndCannotReplay()throws Exception{var path=dir.resolve("claims.json");var j=RewardJournal.open(path);var p=UUID.randomUUID();var id=reserve(j,p);var port=new Port();port.confirmed=false;BatchDelivery.deliver(j,p,id,port);assertTrue(j.get(id).review());BatchDelivery.deliver(RewardJournal.open(path),p,id,port);assertEquals(1,port.commits);assertEquals(2,port.ground);}
 @Test void cancelledGroundSpawnNeverRetriesAppliedInventory()throws Exception{var path=dir.resolve("claims.json");var j=RewardJournal.open(path);var p=UUID.randomUUID();var id=reserve(j,p);var port=new Port();port.fail=1;BatchDelivery.deliver(j,p,id,port);assertTrue(j.get(id).review());assertEquals(1,port.inventory);BatchDelivery.deliver(RewardJournal.open(path),p,id,port);assertEquals(1,port.inventory);assertEquals(0,port.ground);}
 @Test void unavailablePlayerHasNoMutation()throws Exception{var j=RewardJournal.open(dir.resolve("claims.json"));var p=UUID.randomUUID();var id=reserve(j,p);var port=new Port(){public String unavailable(RewardClaim.Part part){return "player_unavailable";}};BatchDelivery.deliver(j,p,id,port);assertEquals(0,port.inventory);assertEquals(0,port.commits);assertFalse(j.get(id).review());}
}
