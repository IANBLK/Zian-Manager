package com.ianblk.zianmanager.core;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.Path;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
class NpcButtonsTest {
 @TempDir Path dir;
 @Test void buttonsHaveIndependentCooldowns()throws Exception{var j=new NpcCommands(dir.resolve("npc.json"));UUID n=UUID.randomUUID(),p=UUID.randomUUID();j.begin(n,p,"main",60,1000);j.complete(n,p,"main",true,1000);assertDoesNotThrow(()->j.begin(n,p,"bonus",60,1001));assertThrows(IllegalArgumentException.class,()->j.begin(n,p,"main",60,1001));}
 @Test void legacyMainCooldownStillAppliesAfterRestart()throws Exception{var file=dir.resolve("npc.json");var j=new NpcCommands(file);UUID n=UUID.randomUUID(),p=UUID.randomUUID();j.begin(n,p,60,1000);j.complete(n,p,true,1000);assertThrows(IllegalArgumentException.class,()->new NpcCommands(file).begin(n,p,"main",60,1001));assertDoesNotThrow(()->new NpcCommands(file).begin(n,p,"extra",60,1001));}
 @Test void failedButtonDoesNotBlockAnotherAction()throws Exception{var j=new NpcCommands(dir.resolve("npc.json"));UUID n=UUID.randomUUID(),p=UUID.randomUUID();j.begin(n,p,"broken",0,1000);j.complete(n,p,"broken",false,1000);assertThrows(IllegalArgumentException.class,()->j.begin(n,p,"broken",0,2000));assertDoesNotThrow(()->j.begin(n,p,"safe",0,2000));}
 @Test void duplicateButtonIdsAndInvalidCommandsRejected(){var b=new Definitions.NpcButton("bonus","Botón","minecraft:give {player} minecraft:bread 1",60);assertThrows(IllegalArgumentException.class,()->new Definitions.NpcSpec(UUID.randomUUID(),"minecraft:overworld",new Definitions.Point(0,64,0,0),"Guía","Hola","heraldo_real","Principal","",60,List.of(b,b)));assertThrows(IllegalArgumentException.class,()->new Definitions.NpcButton("main","Botón","say hello",0));assertThrows(IllegalArgumentException.class,()->new Definitions.NpcButton("bad","Botón","say hello\nsay extra",0));}
}
