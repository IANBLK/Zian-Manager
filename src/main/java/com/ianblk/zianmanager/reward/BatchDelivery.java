package com.ianblk.zianmanager.reward;
import java.util.*;
/** Persist intent before mutation, commit inventory/world once, and never replay uncertain outputs. */
public final class BatchDelivery {
 public interface Port {String unavailable(RewardClaim.Part part);void apply(RewardClaim.Part part,int index)throws Exception;boolean commit()throws Exception;}
 private BatchDelivery(){}
 public static void deliver(RewardJournal journal,UUID player,UUID id,Port port)throws java.io.IOException{synchronized(journal){journal.ensureHealthy();var claim=journal.get(id);if(claim==null || !claim.player().equals(player))throw new IllegalArgumentException("Reclamación inexistente");if(claim.review() || claim.complete())return;var active=new ArrayList<Integer>();try{
  for(int i=0;i<claim.parts().size();i++){var part=journal.get(id).parts().get(i);if(part.phase()==RewardClaim.Phase.DELIVERED)continue;String unavailable=port.unavailable(part);if(unavailable!=null){journal.phase(id,i,RewardClaim.Phase.PENDING,unavailable);break;}journal.phase(id,i,RewardClaim.Phase.APPLYING,"direct_delivery_started");active.add(i);port.apply(part,i);}
  if(active.isEmpty())return;boolean confirmed=port.commit();for(int i:active)journal.phase(id,i,confirmed?RewardClaim.Phase.DELIVERED:RewardClaim.Phase.REVIEW_REQUIRED,confirmed?"inventory_and_drops_confirmed":"direct_delivery_unconfirmed");
 }catch(Exception | LinkageError error){for(int i:active)if(journal.get(id).parts().get(i).phase()!=RewardClaim.Phase.DELIVERED)journal.phase(id,i,RewardClaim.Phase.REVIEW_REQUIRED,"direct_delivery_unconfirmed");}}}
}
