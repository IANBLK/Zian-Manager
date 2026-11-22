package com.ianblk.zianmanager.permission;
import com.ianblk.zianmanager.core.Definitions.NpcButton;
import com.ianblk.zianmanager.core.Definitions.NpcSpec;
import com.ianblk.zianmanager.core.Definitions.Point;
import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.UUID;
import static com.ianblk.zianmanager.permission.LuckPermsLookup.Decision.*;
import static org.junit.jupiter.api.Assertions.*;
class NpcCommandsPermissionTest {
 @Test void decision(){assertTrue(ManagerPermissions.npcCommandsDecision(true,ABSENT));assertTrue(ManagerPermissions.npcCommandsDecision(true,UNDEFINED));assertTrue(ManagerPermissions.npcCommandsDecision(true,TRUE));assertFalse(ManagerPermissions.npcCommandsDecision(true,FALSE));assertFalse(ManagerPermissions.npcCommandsDecision(false,TRUE));assertFalse(ManagerPermissions.npcCommandsDecision(false,ABSENT));}
 static NpcSpec npc(String name,String cmd,List<NpcButton> extra){return new NpcSpec(UUID.fromString("00000000-0000-0000-0000-000000000001"),"minecraft:overworld",new Point(0,70,0,0),name,"Hola","heraldo_real","Ir",cmd,60,extra);}
 @Test void commandChangeDetection(){var old=npc("A","say hi",List.of(new NpcButton("b1","B","say b",0)));
  assertFalse(NpcSpec.commandsChanged(old,npc("Otro","say hi",List.of(new NpcButton("b1","Etiqueta","say b",5)))));
  assertTrue(NpcSpec.commandsChanged(old,npc("A","op x",old.extraActions())));
  assertTrue(NpcSpec.commandsChanged(old,npc("A","say hi",List.of(new NpcButton("b1","B","op x",0)))));
  assertTrue(NpcSpec.commandsChanged(null,npc("A","say hi",List.of())));
  assertFalse(NpcSpec.commandsChanged(null,npc("A","@entrada dun",List.of())));
  assertFalse(NpcSpec.commandsChanged(null,npc("A","",List.of())));}
}
