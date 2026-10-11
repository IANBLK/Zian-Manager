package com.ianblk.zianmanager.reward;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.Path;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
class ZeroWaitKeyTest {
 @TempDir Path dir;
 @Test void zeroWaitRepeatsNewCyclesAndPendingNeverRerolls()throws Exception{var file=dir.resolve("claims.json");var j=RewardJournal.open(file);UUID p=UUID.randomUUID();var reward=new RewardDefinition("",0,List.of("item"),RewardDefinition.Mode.REPEAT,0);assertTrue(j.reserveAt(p,"chest.test",reward,1000,UUID.randomUUID()));var first=j.latest(p,"chest.test");assertFalse(j.reserveAt(p,"chest.test",reward,1000,UUID.randomUUID()));RewardDelivery.deliver(j,p,first.id(),new RewardDelivery.Port(){public String unavailable(RewardClaim.Part part){return null;}public RewardDelivery.Result apply(RewardClaim.Part part){return RewardDelivery.Result.APPLIED;}});j=RewardJournal.open(file);assertTrue(j.reserveAt(p,"chest.test",reward,System.currentTimeMillis(),UUID.randomUUID()));assertEquals(1,j.latest(p,"chest.test").cycle());assertNotEquals(first.id(),j.latest(p,"chest.test").id());}
 @Test void changingToZeroWaitBypassesPreviouslyConfiguredTimer()throws Exception{var j=RewardJournal.open(dir.resolve("claims.json"));UUID p=UUID.randomUUID();j.reserveAt(p,"chest.test",new RewardDefinition("",0,List.of("item"),RewardDefinition.Mode.REPEAT,10),System.currentTimeMillis(),UUID.randomUUID());var claim=j.latest(p,"chest.test");RewardDelivery.deliver(j,p,claim.id(),new RewardDelivery.Port(){public String unavailable(RewardClaim.Part part){return null;}public RewardDelivery.Result apply(RewardClaim.Part part){return RewardDelivery.Result.APPLIED;}});assertEquals(0,j.remaining(p,"chest.test",new RewardDefinition("",0,List.of("item"),RewardDefinition.Mode.REPEAT,0),System.currentTimeMillis()));}
}
