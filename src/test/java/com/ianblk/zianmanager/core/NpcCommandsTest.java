package com.ianblk.zianmanager.core;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.Path;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;
class NpcCommandsTest {
 @TempDir Path dir;
 @Test void successfulCooldownSurvivesRestart()throws Exception{UUID n=UUID.randomUUID(),p=UUID.randomUUID();var file=dir.resolve("npc.json");var journal=new NpcCommands(file);journal.begin(n,p,60,1000);journal.complete(n,p,true,2000);var restarted=new NpcCommands(file);assertThrows(IllegalArgumentException.class,()->restarted.begin(n,p,60,3000));assertDoesNotThrow(()->restarted.begin(n,p,60,62000));}
 @Test void ambiguousExecutionCannotReplay()throws Exception{UUID n=UUID.randomUUID(),p=UUID.randomUUID();var file=dir.resolve("npc.json");new NpcCommands(file).begin(n,p,0,1000);assertThrows(IllegalArgumentException.class,()->new NpcCommands(file).begin(n,p,0,100000));}
 @Test void playersHaveIndependentActions()throws Exception{UUID n=UUID.randomUUID(),p=UUID.randomUUID();var journal=new NpcCommands(dir.resolve("npc.json"));journal.begin(n,p,60,1000);assertDoesNotThrow(()->journal.begin(n,UUID.randomUUID(),60,1000));}
 @Test void legacyNpcDefaultsToSuppliedSkin(){var npc=new com.google.gson.Gson().fromJson("{\"uuid\":\"00000000-0000-0000-0000-000000000114\",\"dimension\":\"minecraft:overworld\",\"point\":{\"x\":0,\"y\":70,\"z\":0,\"yaw\":0},\"name\":\"Guide\",\"text\":\"Hello\"}",Definitions.NpcSpec.class);assertEquals("heraldo_real",npc.skin());assertEquals("",npc.command());}
}
