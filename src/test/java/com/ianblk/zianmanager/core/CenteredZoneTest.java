package com.ianblk.zianmanager.core;
import com.ianblk.zianmanager.core.Definitions.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.Path;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
class CenteredZoneTest {
 @TempDir Path dir;
 @Test void radiusThreeIncludesCenterAndThreeBlocksEachSide(){var c=new Point(10,64,20,0);var b=CenteredZone.bounds(c,3,3,6,0);assertEquals(7,b.second().x()-b.first().x()+1);assertEquals(7,b.second().z()-b.first().z()+1);var z=new ZoneSpec("room","minecraft:overworld",b.first(),b.second(),List.of(),List.of(),1,5,60,"",false,c,3,3,6,0);assertTrue(z.contains("minecraft:overworld",13.9,65,23.9));assertFalse(z.contains("minecraft:overworld",14,65,20));}
 @Test void resizeKeepsOriginAndSupportsAsymmetricVerticalRange(){var c=new Point(-10,70,-20,0);var b=CenteredZone.bounds(c,5,4,8,2);assertEquals(-15,b.first().x());assertEquals(-5,b.second().x());assertEquals(68,b.first().y());assertEquals(77,b.second().y());}
 @Test void centeredConfigurationSurvivesRestart()throws Exception{var c=new Point(10,64,20,0);var b=CenteredZone.bounds(c,3,3,6,0);var path=dir.resolve("defs.json");var store=new ManagerStore(path);store.put(new ZoneSpec("room","minecraft:overworld",b.first(),b.second(),List.of(),List.of(),1,5,60,"",false,c,3,3,6,0));var loaded=new ManagerStore(path).data().zones().get("room");assertEquals(c,loaded.center());assertEquals(3,loaded.radiusX());}
 @Test void legacyZonesLoadWithoutChangingBounds(){var z=new com.google.gson.Gson().fromJson("{\"id\":\"room\",\"dimension\":\"minecraft:overworld\",\"first\":{\"x\":0,\"y\":64,\"z\":0},\"second\":{\"x\":10,\"y\":70,\"z\":10},\"points\":[],\"team\":[],\"waves\":1,\"pauseSeconds\":5,\"respawnSeconds\":60,\"completionLoot\":\"\",\"enabled\":false}",ZoneSpec.class);assertNull(z.center());assertEquals(10,z.second().x());}
 @Test void impossibleSizesRejected(){assertThrows(IllegalArgumentException.class,()->CenteredZone.bounds(new Point(0,64,0,0),65,3,6,0));assertThrows(IllegalArgumentException.class,()->CenteredZone.bounds(new Point(0,64,0,0),3,3,60,10));}
}
