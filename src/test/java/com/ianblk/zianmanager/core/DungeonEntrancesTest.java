package com.ianblk.zianmanager.core;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.Path;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class DungeonEntrancesTest {
  @TempDir Path dir;
  private DungeonEntrances.Arrival point(String id, int x) {
    return new DungeonEntrances.Arrival(id,"Entrada "+id,"minecraft:overworld",new Definitions.Point(x,64,0,90));
  }
  @Test void renameAndRecapturePreserveDungeonIdentityAndOtherDungeons() throws Exception {
    var path=dir.resolve("entrances.json");var store=new DungeonEntrances(path);
    store.save("crypt","Cripta");store.save("mine","Mina en el mismo mundo");
    store.capture("crypt","Cripta",point("north",0),false);store.capture("crypt","Cripta",point("south",100),false);
    store.capture("mine","Mina",point("door",200),false);
    store.save("crypt","Cripta renovada");store.capture("crypt","Cripta renovada",point("north",10),true);
    store=new DungeonEntrances(path);
    assertEquals("Cripta renovada",store.get("crypt").name());assertEquals(2,store.get("crypt").points().size());
    assertEquals(10,store.get("crypt").points().stream().filter(p->p.id().equals("north")).findFirst().orElseThrow().position().x());
    assertEquals(200,store.get("mine").points().getFirst().position().x());
    store.removePoint("crypt","south");assertEquals(1,new DungeonEntrances(path).get("crypt").points().size());
  }
  @Test void invalidReferencesAndLimitsDoNotPublishOrReplaceStoredData() throws Exception {
    var path=dir.resolve("entrances.json");var store=new DungeonEntrances(path);store.save("crypt","Cripta");
    assertThrows(IllegalArgumentException.class,()->store.capture("crypt","Cripta",point("missing",0),true));
    assertThrows(IllegalArgumentException.class,()->store.put(new DungeonEntrances.Dungeon("crypt","Cripta",Collections.nCopies(2,point("duplicate",0)))));
    for(int i=0;i<32;i++)store.capture("crypt","Cripta",point("p_"+i,i),false);
    assertThrows(IllegalArgumentException.class,()->store.capture("crypt","Cripta",point("overflow",99),false));
    assertEquals(32,new DungeonEntrances(path).get("crypt").points().size());
    assertThrows(IllegalArgumentException.class,()->DungeonEntrances.target("@entrada crypt extra"));
    assertEquals("crypt",DungeonEntrances.target("@entrada crypt"));assertEquals("",DungeonEntrances.target("tp {player} 1 2 3"));
  }
}
