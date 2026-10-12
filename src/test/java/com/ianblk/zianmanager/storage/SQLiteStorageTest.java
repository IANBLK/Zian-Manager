package com.ianblk.zianmanager.storage;

import java.nio.file.*;
import java.nio.channels.*;
import java.nio.ByteBuffer;
import java.util.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.*;

class SQLiteStorageTest {
  @TempDir Path world;
  Path root(){return world.resolve("data/zianmanager");}
  @AfterEach void close()throws Exception{StorageFiles.closeAll();}
  private void moveOriginals()throws Exception{
    Path source=root().toAbsolutePath().normalize(),target=world.resolve("originals").toAbsolutePath().normalize();
    assertTrue(source.startsWith(world.toAbsolutePath().normalize()));assertTrue(target.startsWith(world.toAbsolutePath().normalize()));
    for(int attempt=0;;attempt++)try{Files.move(source,target);return;}catch(AccessDeniedException busy){if(attempt>=8)throw busy;Thread.sleep(25L*(attempt+1));}
  }
  private void migrate()throws Exception{StorageMaintenance.migrateOffline(world);moveOriginals();}

  @Test void twoThousandPlayersImportWithoutAnyPlayerFilesAndReopenLosslessly()throws Exception{
    Files.createDirectories(root());
    for(int i=0;i<2000;i++)Files.writeString(root().resolve(new UUID(0,i)+".json"),"player="+i+" " +"x".repeat(6000));
    var result=StorageMaintenance.migrateOffline(world);assertEquals(2000,result.files());
    moveOriginals();
    for(int pass=0;pass<2;pass++){
      assertFalse(Files.exists(root()));
      try(var listing=StorageFiles.list(root())){assertEquals(2000,listing.count());}
      for(int i:List.of(0,999,1999))assertEquals("player="+i+" "+"x".repeat(6000),StorageFiles.readString(root().resolve(new UUID(0,i)+".json")));
      StorageFiles.closeAll();
    }
  }
  @Test void journalLocksAtomicReplaceAndForceSurviveReopening()throws Exception{
    Files.createDirectories(root());Files.writeString(root().resolve("journal.wal"),"before");migrate();
    Path journal=root().resolve("journal.wal");
    try(var channel=StorageFiles.channel(journal,StandardOpenOption.READ,StandardOpenOption.WRITE);var lock=channel.tryLock()){
      try(var other=StorageFiles.channel(journal,StandardOpenOption.READ,StandardOpenOption.WRITE)){assertThrows(OverlappingFileLockException.class,other::tryLock);}
      channel.position(channel.size());channel.write(ByteBuffer.wrap("-durable".getBytes()));channel.force(true);
    }
    StorageFiles.closeAll();assertEquals("before-durable",StorageFiles.readString(journal));
    Path temporary=StorageFiles.createTempFile(root(),"replace-",".tmp");StorageFiles.writeString(temporary,"new");
    StorageFiles.move(temporary,journal,StandardCopyOption.REPLACE_EXISTING,StandardCopyOption.ATOMIC_MOVE);
    StorageFiles.closeAll();assertEquals("new",StorageFiles.readString(journal));assertFalse(StorageFiles.exists(temporary));
  }
  @Test void runningWorldAndMissingDatabaseNeverPublishOrFallBack()throws Exception{
    Files.createDirectories(root());Path file=root().resolve("test.json");Files.writeString(file,"original");
    try(var channel=FileChannel.open(world.resolve("session.lock"),StandardOpenOption.CREATE,StandardOpenOption.WRITE);var lock=channel.tryLock()){
      assertThrows(java.io.IOException.class,()->StorageMaintenance.migrateOffline(world));
      assertFalse(Files.exists(world.resolve("zian-storage/manager.db")));
    }
    StorageMaintenance.migrateOffline(world);StorageFiles.closeAll();
    Files.move(world.resolve("zian-storage/manager.db"),world.resolve("saved.db"));
    assertThrows(java.io.IOException.class,()->StorageFiles.readString(file));assertEquals("original",Files.readString(file));
  }
  @Test void corruptionLocksStorageInsteadOfReadingStaleLegacyCopy()throws Exception{
    Files.createDirectories(root());Path file=root().resolve("test.json");Files.writeString(file,"essential record");StorageMaintenance.migrateOffline(world);
    try(var connection=StorageFiles.connect(world.resolve("zian-storage/manager.db"));var s=connection.createStatement()){s.execute("update pages set body=zeroblob(4096)");}
    assertThrows(java.io.IOException.class,()->StorageFiles.readString(file));
    assertThrows(java.io.IOException.class,()->StorageFiles.writeString(file,"must not write"));
    assertEquals("essential record",Files.readString(file));
  }
  @Test void malformedUtf8IsRejectedWithoutSilentlyChangingStoredBytes()throws Exception{
    Files.createDirectories(root());Path path=root().resolve("bad.json");byte[] raw=new byte[]{(byte)0xc3,(byte)0xfe};Files.write(path,raw);migrate();
    assertArrayEquals(raw,StorageFiles.readAllBytes(path));assertThrows(java.nio.charset.CharacterCodingException.class,()->StorageFiles.readString(path));
  }
  @Test void coldDataCompressionPreservesPendingAndCompletedRecordsByteForByte()throws Exception{
    Files.createDirectories(root());Path file=root().resolve("receipts.json");byte[] data=("PENDING REVIEW DELIVERED ".repeat(2000)).getBytes();Files.write(file,data);
    Files.setLastModifiedTime(file,java.nio.file.attribute.FileTime.fromMillis(System.currentTimeMillis()-120L*86400000));migrate();
    var preview=StorageMaintenance.cleanupOffline(world,false);assertTrue(preview.compressedPages()>0);
    StorageMaintenance.cleanupOffline(world,true);
    assertArrayEquals(data,StorageFiles.readAllBytes(file));StorageFiles.closeAll();assertArrayEquals(data,StorageFiles.readAllBytes(file));
  }
  @Test void incompleteRestoreCannotRecreateMissingDedupTables()throws Exception{
    Files.createDirectories(root());Path path=root().resolve("proof.json");Files.writeString(path,"essential");StorageMaintenance.migrateOffline(world);
    try(var connection=StorageFiles.connect(world.resolve("zian-storage/manager.db"));var statement=connection.createStatement()){statement.execute("drop table receipts");}
    assertThrows(java.io.IOException.class,()->StorageFiles.readString(path));assertEquals("essential",Files.readString(path));
  }
}
