package com.ianblk.zianmanager;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.*;
import net.minecraft.world.level.Level;
import net.minecraft.network.syncher.*;
import net.minecraft.nbt.CompoundTag;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.*;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
public final class ManagerNpcs {
    private static final DeferredRegister<EntityType<?>> TYPES=DeferredRegister.create(Registries.ENTITY_TYPE,"zianmanager");
    public static final DeferredHolder<EntityType<?>,EntityType<DialogueNpc>> NPC=TYPES.register("dialogue_npc",()->EntityType.Builder.of(DialogueNpc::new,MobCategory.MISC).sized(0.6f,1.8f).clientTrackingRange(8).build("zianmanager:dialogue_npc"));
    public static void register(IEventBus bus){TYPES.register(bus);bus.addListener((EntityAttributeCreationEvent e)->e.put(NPC.get(),Mob.createMobAttributes().build()));}
    public static final class DialogueNpc extends PathfinderMob {
        private static final EntityDataAccessor<String> SKIN=SynchedEntityData.defineId(DialogueNpc.class,EntityDataSerializers.STRING);
        public DialogueNpc(EntityType<? extends PathfinderMob> type,Level level){super(type,level);setNoAi(true);setInvulnerable(true);setNoGravity(true);setPersistenceRequired();}
        @Override protected void defineSynchedData(SynchedEntityData.Builder builder){super.defineSynchedData(builder);builder.define(SKIN,"heraldo_real");}
        public String skin(){return entityData.get(SKIN);}
        public void skin(String value){entityData.set(SKIN,value);}
        @Override public void addAdditionalSaveData(CompoundTag tag){super.addAdditionalSaveData(tag);tag.putString("ZianSkin",skin());}
        @Override public void readAdditionalSaveData(CompoundTag tag){super.readAdditionalSaveData(tag);if(tag.contains("ZianSkin"))skin(tag.getString("ZianSkin"));}
    }
}
