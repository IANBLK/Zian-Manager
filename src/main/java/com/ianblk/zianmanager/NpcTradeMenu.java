package com.ianblk.zianmanager;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.registries.*;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import java.util.*;

/** Read-only offer previews; the server exchanges the entire bundle from the player's inventory. */
public final class NpcTradeMenu extends AbstractContainerMenu {
 private static final DeferredRegister<MenuType<?>> MENUS=DeferredRegister.create(Registries.MENU,"zianmanager");
 public static final DeferredHolder<MenuType<?>,MenuType<NpcTradeMenu>> TYPE=MENUS.register("npc_trade",()->IMenuTypeExtension.create(NpcTradeMenu::new));
 public static void register(net.neoforged.bus.api.IEventBus bus){MENUS.register(bus);}
 private static final java.lang.reflect.Field HYBRID_OWNER=hybridOwnerField();
 private static java.lang.reflect.Field hybridOwnerField(){try{return AbstractContainerMenu.class.getField("containerOwner");}catch(NoSuchFieldException pureNeoForge){return null;}}
 private void bindHybridOwner(Player p){if(p!=null&&HYBRID_OWNER!=null)try{HYBRID_OWNER.set(this,p);}catch(IllegalAccessException failure){throw new IllegalStateException("No se pudo preparar la ventana de comercio",failure);}}
 private final ManagerNpcs.DialogueNpc npc;private final long revision;private final Player owner;private final List<NpcTrading.Stacks> rows;private final List<String> labels;
 private final SimpleContainer previews=new SimpleContainer(8){@Override public int getMaxStackSize(){return 99;}};
 private final DataSlot selection=DataSlot.standalone(),status=DataSlot.standalone();
 public NpcTradeMenu(int id,Inventory inventory,RegistryFriendlyByteBuf buffer){this(id,inventory,null,0,List.of(),readLabels(buffer));}
 public NpcTradeMenu(int id,Inventory inventory,ManagerNpcs.DialogueNpc npc,long revision,List<NpcTrading.Stacks> rows,List<String> labels){
  super(TYPE.get(),id);this.owner=inventory.player;bindHybridOwner(owner);this.npc=npc;this.revision=revision;this.rows=List.copyOf(rows);this.labels=List.copyOf(labels);if(labels.size()>64)throw new IllegalArgumentException("Demasiadas ofertas");
  for(int i=0;i<8;i++){int index=i;addSlot(new Slot(previews,index,164+(i%4)*40,i<4?43:79){@Override public boolean mayPlace(ItemStack stack){return false;}@Override public boolean mayPickup(Player p){return false;}});}
  for(int i=0;i<3;i++)for(int j=0;j<9;j++)addSlot(new Slot(inventory,j+i*9+9,150+j*18,143+i*18));
  for(int j=0;j<9;j++)addSlot(new Slot(inventory,j,150+j*18,201));
  addDataSlot(selection);addDataSlot(status);if(!rows.isEmpty())select(0);
 }
 private static List<String> readLabels(RegistryFriendlyByteBuf buffer){if(buffer==null)return List.of();int count=buffer.readVarInt();if(count<1||count>64)throw new IllegalArgumentException("Catálogo inválido");var labels=new ArrayList<String>();for(int i=0;i<count;i++)labels.add(buffer.readUtf(96));return List.copyOf(labels);}
 public UUID npcId(){return npc==null?null:npc.getUUID();}public List<String> labels(){return labels;}public int selected(){return selection.get();}public int status(){return status.get();}
 private void select(int index){selection.set(index);status.set(0);var row=rows.get(index);for(int i=0;i<8;i++){var side=i<4?row.payments():row.results();int at=i%4;previews.setItem(i,at<side.size()?side.get(at).copy():ItemStack.EMPTY);}}
 @Override public boolean stillValid(Player p){if(p.level().isClientSide)return true;return p==owner&&!p.isSpectator()&&p.isAlive()&&npc!=null&&npc.isAlive()&&npc.level()==p.level()&&npc.distanceToSqr(p)<=36&&p instanceof ServerPlayer server&&NpcTrading.allowed(server)&&NpcTrading.get().revision(npc.getUUID())==revision&&ManagerRuntime.get().store().data().npcs().containsKey(npc.getUUID());}
 @Override public boolean clickMenuButton(Player p,int button){
  if(!(p instanceof ServerPlayer server)||!stillValid(p)){status.set(4);return false;}
  if(button>=0&&button<rows.size()){select(button);broadcastChanges();return true;}
  if(button!=64||selection.get()<0||selection.get()>=rows.size())return false;
  var row=rows.get(selection.get());var before=new ArrayList<ItemStack>();for(int i=0;i<36;i++)before.add(p.getInventory().getItem(i).copy());
  var plan=InventoryTradePlan.plan(before,row.payments(),row.results(),p.getInventory().getMaxStackSize());
  if(plan.failure()!=InventoryTradePlan.Failure.NONE){status.set(plan.failure()==InventoryTradePlan.Failure.PAYMENT?2:3);broadcastChanges();return false;}
  if(!stillValid(p)){status.set(4);return false;}for(int i=0;i<36;i++)if(!ItemStack.matches(before.get(i),p.getInventory().getItem(i))){status.set(5);return false;}
  for(int i=0;i<36;i++)p.getInventory().setItem(i,plan.inventory().get(i).copy());p.getInventory().setChanged();status.set(1);broadcastChanges();return true;
 }
 @Override public void clicked(int slot,int button,ClickType type,Player p){if(slot>=0&&slot<8)return;if(stillValid(p))super.clicked(slot,button,type,p);}
 @Override public ItemStack quickMoveStack(Player p,int index){if(!stillValid(p)||index<8||index>=slots.size())return ItemStack.EMPTY;var slot=slots.get(index);if(!slot.hasItem())return ItemStack.EMPTY;var stack=slot.getItem();var copy=stack.copy();if(!moveItemStackTo(stack,index<35?35:8,index<35?44:35,false))return ItemStack.EMPTY;if(stack.isEmpty())slot.setByPlayer(ItemStack.EMPTY);else slot.setChanged();slot.onTake(p,stack);return copy;}
}
