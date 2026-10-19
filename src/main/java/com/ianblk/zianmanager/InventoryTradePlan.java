package com.ianblk.zianmanager;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.component.DataComponentPredicate;
import java.util.*;

/** Plans on copies: neither a missing payment nor a full inventory can partially execute a trade. */
public final class InventoryTradePlan {
 public enum Failure { NONE, PAYMENT, SPACE }
 public record Plan(Failure failure,List<ItemStack> inventory){}
 private InventoryTradePlan(){}
 public static Plan plan(List<ItemStack> inventory,List<ItemStack> payments,List<ItemStack> results,int slotLimit){
  if(payments.isEmpty()||payments.size()>4||results.isEmpty()||results.size()>4||slotLimit<1)throw new IllegalArgumentException("Oferta inválida");
  var next=inventory.stream().map(ItemStack::copy).collect(java.util.stream.Collectors.toCollection(ArrayList::new));
  // Small integral flow avoids consuming a specially named item for a generic cost
  // when that same item is needed by another, more specific, price in the bundle.
  int costs=payments.size(),firstSlot=1+costs,sink=firstSlot+next.size(),nodes=sink+1,total=0;
  int[][] residual=new int[nodes][nodes];
  for(int i=0;i<costs;i++){var cost=payments.get(i);if(cost.isEmpty()||cost.getCount()<1||cost.getCount()>cost.getMaxStackSize())throw new IllegalArgumentException("Pago vacío");residual[0][1+i]=cost.getCount();total+=cost.getCount();var predicate=DataComponentPredicate.allOf(cost.getComponents());for(int j=0;j<next.size();j++){var stack=next.get(j);if(!stack.isEmpty()&&stack.is(cost.getItem())&&predicate.test(stack))residual[1+i][firstSlot+j]=cost.getCount();}}
  for(int i=0;i<next.size();i++)residual[firstSlot+i][sink]=next.get(i).getCount();
  int delivered=0;
  while(delivered<total){int[] parent=new int[nodes];Arrays.fill(parent,-1);parent[0]=0;var queue=new ArrayDeque<Integer>();queue.add(0);while(!queue.isEmpty()&&parent[sink]<0){int from=queue.remove();for(int to=1;to<nodes;to++)if(parent[to]<0&&residual[from][to]>0){parent[to]=from;queue.add(to);}}
   if(parent[sink]<0)return new Plan(Failure.PAYMENT,List.of());int amount=total-delivered;for(int at=sink;at!=0;at=parent[at])amount=Math.min(amount,residual[parent[at]][at]);for(int at=sink;at!=0;at=parent[at]){residual[parent[at]][at]-=amount;residual[at][parent[at]]+=amount;}delivered+=amount;
  }
  for(int i=0;i<next.size();i++)next.get(i).shrink(next.get(i).getCount()-residual[firstSlot+i][sink]);
  for(var output:results){if(output.isEmpty()||output.getCount()<1||output.getCount()>output.getMaxStackSize())throw new IllegalArgumentException("Resultado vacío");int remaining=output.getCount(),limit=Math.min(slotLimit,output.getMaxStackSize());
   for(var stack:next)if(!stack.isEmpty()&&ItemStack.isSameItemSameComponents(stack,output)){int put=Math.min(remaining,Math.max(0,limit-stack.getCount()));stack.grow(put);remaining-=put;if(remaining==0)break;}
   for(int i=0;i<next.size()&&remaining>0;i++)if(next.get(i).isEmpty()){int put=Math.min(remaining,limit);next.set(i,output.copyWithCount(put));remaining-=put;}
   if(remaining!=0)return new Plan(Failure.SPACE,List.of());
  }
  return new Plan(Failure.NONE,List.copyOf(next));
 }
}
