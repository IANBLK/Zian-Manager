package com.ianblk.zianmanager;
import com.ianblk.zianmanager.reward.*;
import com.ianblk.zianmanager.core.KeyLedger;
import net.minecraft.server.level.ServerPlayer;
import java.util.*;
/** Local command completion fixture; no production rewards are touched. */
public final class CommandSuggestionsSmoke {
 public static void verify(ManagerRuntime runtime,ServerPlayer first,ServerPlayer second)throws Exception{
  var firstClaim=runtime.loot().reserve(first,"mob.command_"+UUID.randomUUID(),"smoke_mob",0,first.blockPosition());var otherClaim=runtime.loot().reserve(second,"mob.command_"+UUID.randomUUID(),"smoke_mob",0,second.blockPosition());
  var field=LootService.class.getDeclaredField("journal");field.setAccessible(true);var journal=(RewardJournal)field.get(runtime.loot());journal.phase(firstClaim.id(),0,RewardClaim.Phase.APPLYING,"smoke_fixture");
  var keyField=LootService.class.getDeclaredField("keys");keyField.setAccessible(true);var keys=(KeyLedger)keyField.get(runtime.loot());keys.bind(firstClaim.id(),first.getUUID(),"zianmanager:common_key");try{keys.take(firstClaim.id(),first.getUUID(),new KeyLedger.Port(){public boolean available(String id){return true;}public boolean consumeAndSave(String id){return false;}});}catch(IllegalArgumentException expected){}
  var server=first.getServer();server.getPlayerList().op(first.getGameProfile());try{
   var commands=server.getCommands().getDispatcher();var source=first.createCommandSourceStack();
   for(String command:List.of("zianmanager claim ","zianmanager resolve "+first.getGameProfile().getName()+" ","zianmanager resolvekey "+first.getGameProfile().getName()+" ")){var suggestions=commands.getCompletionSuggestions(commands.parse(command,source)).get().getList().stream().map(s->s.getText()).toList();if(!suggestions.contains(firstClaim.id().toString()) || suggestions.contains(otherClaim.id().toString()))throw new IllegalStateException("Reward completion missing or exposes another player's reward: "+command);}
   String part="zianmanager resolve "+first.getGameProfile().getName()+" "+firstClaim.id()+" ";var suggestions=commands.getCompletionSuggestions(commands.parse(part,source)).get().getList().stream().map(s->s.getText()).toList();if(!suggestions.equals(List.of("1")))throw new IllegalStateException("Review parts completion includes incorrect parts");
  }finally{server.getPlayerList().deop(first.getGameProfile());}
  runtime.loot().resolveKey(firstClaim.id(),first.getUUID(),first.getUUID(),"smoke fixture with no key debit");runtime.loot().resolve(first.getUUID(),firstClaim.id(),0,first.getUUID(),"smoke fixture with no external mutation");runtime.loot().deliver(first,firstClaim.id());runtime.loot().deliver(second,otherClaim.id());ZianManager.LOGGER.info("Zian Manager command completion smoke passed: owned pending IDs, review IDs, key review IDs and affected parts");
 }
}
