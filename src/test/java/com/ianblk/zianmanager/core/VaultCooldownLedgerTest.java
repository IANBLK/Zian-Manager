package com.ianblk.zianmanager.core;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.*;
import java.util.*;
import java.io.IOException;
import static org.junit.jupiter.api.Assertions.*;

class VaultCooldownLedgerTest {
 @TempDir Path dir;
 final UUID alice=UUID.randomUUID(),bob=UUID.randomUUID();
 final VaultCooldownLedger.Vault vault=new VaultCooldownLedger.Vault("minecraft:overworld",1,60,2);
 @Test void restartKeepsDeadlineAndIsolatesPlayersAndWorlds()throws Exception{
  var ledger=new VaultCooldownLedger(dir);ledger.reserve(vault,alice,1000,1800000);
  var restored=new VaultCooldownLedger(dir);
  assertEquals(1799999,restored.remaining(vault,alice,1001));assertEquals(0,restored.remaining(vault,bob,1001));
  assertEquals(0,restored.remaining(new VaultCooldownLedger.Vault("minecraft:the_nether",1,60,2),alice,1001));
  assertEquals(0,restored.remaining(new VaultCooldownLedger.Vault("minecraft:overworld",2,60,2),alice,1001));
 }
 @Test void exactExpiryAndConcurrentClickOnlyOneReservation()throws Exception{
  var ledger=new VaultCooldownLedger(dir);ledger.reserve(vault,alice,1000,1000);
  assertThrows(IllegalStateException.class,()->ledger.reserve(vault,alice,1999,1000));
  assertEquals(1,ledger.remaining(vault,alice,1999));assertEquals(0,ledger.remaining(vault,alice,2000));
  ledger.reserve(vault,alice,2000,1000);assertEquals(1000,ledger.remaining(vault,alice,2000));
 }
 @Test void twoPlayersMayOpenSameVault()throws Exception{
  var ledger=new VaultCooldownLedger(dir);ledger.reserve(vault,alice,1000,1000);ledger.reserve(vault,bob,1000,2000);
  assertEquals(1000,ledger.remaining(vault,alice,1000));assertEquals(2000,ledger.remaining(vault,bob,1000));
 }
 @Test void rollbackOnlyEmptyOrRejectedOpeningRestoresPrevious()throws Exception{
  var ledger=new VaultCooldownLedger(dir);var first=ledger.reserve(vault,alice,1000,1000);ledger.rollback(first);
  assertFalse(ledger.known(vault,alice));ledger.reserve(vault,alice,1000,1000);
  var second=ledger.reserve(vault,alice,2000,1000);ledger.rollback(second);
  assertTrue(ledger.known(vault,alice));assertEquals(0,ledger.remaining(vault,alice,2000));
 }
 @Test void staleRollbackCannotUndoNewOpening()throws Exception{
  var ledger=new VaultCooldownLedger(dir);var old=ledger.reserve(vault,alice,1000,1000);ledger.reserve(vault,alice,2000,1000);
  assertThrows(IOException.class,()->ledger.rollback(old));assertEquals(1000,ledger.remaining(vault,alice,2000));
 }
 @Test void interruptedDeliveryKeepsReservationOnReload()throws Exception{
  new VaultCooldownLedger(dir).reserve(vault,alice,1000,1000);
  assertThrows(IllegalStateException.class,()->new VaultCooldownLedger(dir).reserve(vault,alice,1001,1000));
 }
 @Test void storageFailureBlocksFurtherOpenings(){
  var ledger=new VaultCooldownLedger(dir,(p,d)->{throw new IOException("disk unavailable");});
  assertThrows(IOException.class,()->ledger.reserve(vault,alice,1000,1000));assertFalse(ledger.healthy());
  assertThrows(IOException.class,()->ledger.remaining(vault,bob,1000));
 }
 @Test void corruptionDoesNotResetCooldown()throws Exception{
  Files.writeString(dir.resolve(vault.filename()),"broken");var ledger=new VaultCooldownLedger(dir);
  assertThrows(IOException.class,()->ledger.reserve(vault,alice,1000,1000));assertEquals("broken",Files.readString(dir.resolve(vault.filename())));
 }
 @Test void expiryMasksOnlyThisVaultKnownPlayers()throws Exception{
  var ledger=new VaultCooldownLedger(dir);ledger.reserve(vault,alice,1000,1000);ledger.reserve(vault,bob,1000,2000);UUID legacy=UUID.randomUUID();
  assertEquals(Set.of(alice),ledger.expired(vault,Set.of(alice,bob,legacy),2000));
 }
 @Test void clockRollbackDoesNotReopen()throws Exception{
  var ledger=new VaultCooldownLedger(dir);ledger.reserve(vault,alice,5000,1000);
  assertEquals(5000,ledger.remaining(vault,alice,1000));
 }
 @Test void boundedDurations(){var ledger=new VaultCooldownLedger(dir);assertThrows(IllegalArgumentException.class,()->ledger.reserve(vault,alice,1,0));}
 @Test void adminAdjustmentShortensOnlySelectedPlayerAndPersists()throws Exception{
  var ledger=new VaultCooldownLedger(dir);ledger.reserve(vault,alice,1000,3600000);ledger.reserve(vault,bob,1000,3600000);
  ledger.adjust(vault,alice,2000,60000);
  var restored=new VaultCooldownLedger(dir);assertEquals(60000,restored.remaining(vault,alice,2000));assertEquals(3599000,restored.remaining(vault,bob,2000));
  restored.adjust(vault,alice,2000,0);assertTrue(restored.known(vault,alice));assertEquals(0,restored.remaining(vault,alice,2000));
 }
 @Test void adminAdjustmentAdoptsLegacyWithoutRestartingWait()throws Exception{
  var ledger=new VaultCooldownLedger(dir);ledger.adjust(vault,alice,5000,0);
  assertTrue(ledger.known(vault,alice));assertEquals(Set.of(alice),ledger.expired(vault,Set.of(alice,bob),5000));
  assertThrows(IllegalArgumentException.class,()->ledger.adjust(vault,alice,5000,-1));
 }
 @Test void sqliteRestartRetainsReservations()throws Exception{
  Path world=dir.resolve("world"),data=world.resolve("data/zianmanager"),cooldowns=data.resolve("vault-cooldowns");
  Files.createDirectories(data);com.ianblk.zianmanager.storage.StorageMaintenance.migrateOffline(world);
  try{
   new VaultCooldownLedger(cooldowns).reserve(vault,alice,1000,1800000);
   com.ianblk.zianmanager.storage.StorageFiles.closeAll();
   assertEquals(1799999,new VaultCooldownLedger(cooldowns).remaining(vault,alice,1001));
   assertTrue(Files.isRegularFile(world.resolve("zian-storage/manager.db")));
  }finally{com.ianblk.zianmanager.storage.StorageFiles.closeAll();}
 }
}
