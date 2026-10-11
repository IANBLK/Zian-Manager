package com.ianblk.zianmanager.core;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.*;
class KeyShardsTest {
  @TempDir Path root;
  @Test void paidAndUnconfirmedConsumptionSurviveOwnerMigrationAndCachePressure()throws Exception {
    Path path=root.resolve("keys.json");var old=new KeyLedger(path);UUID player=UUID.randomUUID(),paid=UUID.randomUUID(),review=UUID.randomUUID();
    old.bind(paid,player,"zianmanager:common_key");old.take(paid,player,new KeyLedger.Port(){public boolean available(String key){return true;}public boolean consumeAndSave(String key){return true;}});
    old.bind(review,player,"zianmanager:rare_key");assertThrows(IllegalArgumentException.class,()->old.take(review,player,new KeyLedger.Port(){public boolean available(String key){return true;}public boolean consumeAndSave(String key){return false;}}));
    KeyStorageMaintenance.migrateOffline(path);var ledger=new KeyLedger(path);var calls=new AtomicInteger();
    var port=new KeyLedger.Port(){public boolean available(String key){return true;}public boolean consumeAndSave(String key){calls.incrementAndGet();for(int i=0;i<64;i++)ledger.get(UUID.randomUUID(),UUID.randomUUID());return true;}};
    ledger.take(paid,player,port);assertEquals(0,calls.get());assertThrows(IllegalArgumentException.class,()->ledger.take(review,player,port));assertEquals(0,calls.get());
    UUID next=UUID.randomUUID();ledger.bind(next,player,"zianmanager:common_key");ledger.take(next,player,port);
    assertEquals("PAID",new KeyLedger(path).get(player,next).phase());assertEquals(1,calls.get());
    assertNull(new KeyLedger(path).get(UUID.randomUUID(),paid));
  }
}
