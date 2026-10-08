package com.ianblk.zianmanager;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.item.*;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.core.*;
import net.minecraft.network.chat.Component;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.server.level.ServerPlayer;
import com.mojang.serialization.MapCodec;
import net.neoforged.neoforge.registries.*;
import net.neoforged.bus.api.IEventBus;
import net.minecraft.nbt.CompoundTag;

public final class ManagerBlocks {
    private static final DeferredRegister.Blocks BLOCKS=DeferredRegister.createBlocks("zianmanager");
    private static final DeferredRegister.Items ITEMS=DeferredRegister.createItems("zianmanager");
    private static final DeferredRegister<BlockEntityType<?>> ENTITIES=DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE,"zianmanager");
    public static final DeferredBlock<DungeonChest> CHEST=BLOCKS.register("dungeon_chest",()->new DungeonChest(BlockBehaviour.Properties.of().strength(-1,3600000).noLootTable().noOcclusion().pushReaction(PushReaction.BLOCK)));
    public static final DeferredItem<BlockItem> CHEST_ITEM=ITEMS.registerSimpleBlockItem(CHEST);
    public static final DeferredHolder<BlockEntityType<?>,BlockEntityType<ChestEntity>> CHEST_ENTITY=ENTITIES.register("dungeon_chest",()->BlockEntityType.Builder.of(ChestEntity::new,CHEST.get()).build(null));
    public static void register(IEventBus bus){BLOCKS.register(bus);ITEMS.register(bus);ENTITIES.register(bus);}
    public static final class ChestEntity extends BlockEntity {
        public ChestEntity(BlockPos pos,BlockState state){super(CHEST_ENTITY.get(),pos,state);}
        // No Container/RandomizableContainer inheritance: Lootr and hoppers do not own this inventory.
    }
    public static final class DungeonChest extends BaseEntityBlock {
        public static final MapCodec<DungeonChest> CODEC=simpleCodec(DungeonChest::new);
        public DungeonChest(Properties p){super(p);}
        @Override protected MapCodec<? extends BaseEntityBlock> codec(){return CODEC;}
        @Override public BlockEntity newBlockEntity(BlockPos pos,BlockState state){return new ChestEntity(pos,state);}
        @Override protected RenderShape getRenderShape(BlockState state){return RenderShape.MODEL;}
        @Override protected net.minecraft.world.phys.shapes.VoxelShape getShape(BlockState state,net.minecraft.world.level.BlockGetter level,BlockPos pos,net.minecraft.world.phys.shapes.CollisionContext context){return Block.box(1,0,1,15,14,15);}
        @Override protected InteractionResult useWithoutItem(BlockState state,Level level,BlockPos pos,Player player,BlockHitResult hit){
            if(player instanceof ServerPlayer target)ManagerRuntime.get().openChest(target,pos);return InteractionResult.sidedSuccess(level.isClientSide);
        }
        @Override public boolean canEntityDestroy(BlockState state,net.minecraft.world.level.BlockGetter level,BlockPos pos,net.minecraft.world.entity.Entity entity){return false;}
    }
}
