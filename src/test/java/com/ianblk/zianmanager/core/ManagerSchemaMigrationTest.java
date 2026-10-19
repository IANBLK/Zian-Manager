package com.ianblk.zianmanager.core;
import com.ianblk.zianmanager.core.Definitions.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
class ManagerSchemaMigrationTest {
 @TempDir Path root;
 @Test void preservesDefinitionsAndBacksUpBeforeChangingOwnershipSchema()throws Exception{var source=new ManagerStore.Data(1,Map.of(),Map.of(),Map.of(),Map.of(),Map.of());var file=root.resolve("definitions.json");AtomicJson.write(file,source);var store=new ManagerStore(file);assertEquals(2,store.data().schema());assertEquals(source,AtomicJson.read(root.resolve("definitions-before-consolidation-v2.json"),ManagerStore.Data.class,null));assertEquals(store.data(),new ManagerStore(file).data());}
 @Test void noOlderSchemaIsWrittenAfterModifyingTheStore()throws Exception{var file=root.resolve("definitions.json");var store=new ManagerStore(file);var mob=new MobSpec("guard","Guardián","minecraft:zombie",20,3,0,0,0,List.of(),Map.of(),"",false);store.put(mob);assertEquals(2,new ManagerStore(file).data().schema());assertEquals(mob,store.data().mobs().get("guard"));}
 @Test void unknownSchemaFailsWithoutReplacingTheSource()throws Exception{var file=root.resolve("definitions.json");String original="{\"schema\":99,\"mobs\":{},\"loot\":{},\"zones\":{},\"chests\":{},\"npcs\":{}}";Files.writeString(file,original);assertThrows(Exception.class,()->new ManagerStore(file));assertEquals(original,Files.readString(file));}
}
