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
    public static final java.util.Map<String,DeferredBlock<DungeonChest>> CRATES=new java.util.LinkedHashMap<>();
    private static final DeferredRegister.Items ITEMS=DeferredRegister.createItems("zianmanager");
    private static final DeferredRegister<BlockEntityType<?>> ENTITIES=DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE,"zianmanager");
    public static final DeferredBlock<DungeonChest> CHEST=BLOCKS.register("dungeon_chest",()->new DungeonChest(BlockBehaviour.Properties.of().strength(-1,3600000).noLootTable().noOcclusion().pushReaction(PushReaction.BLOCK)));
    public static final DeferredItem<BlockItem> CHEST_ITEM=ITEMS.registerSimpleBlockItem(CHEST);
    static {for(String id:java.util.List.of("loot_common_crate","loot_rare_crate","loot_legendary_crate","loot_cosmetic_crate","loot_vote_crate","locked_common_crate","locked_rare_crate","locked_epic_crate","locked_legendary_crate")){var ref=BLOCKS.register(id,()->new DungeonChest(BlockBehaviour.Properties.of().strength(-1,3600000).noLootTable().noOcclusion().pushReaction(PushReaction.BLOCK),id));CRATES.put(id,ref);ITEMS.registerSimpleBlockItem(ref);}}
    public static final DeferredHolder<BlockEntityType<?>,BlockEntityType<ChestEntity>> CHEST_ENTITY=ENTITIES.register("dungeon_chest",()->{var blocks=new java.util.ArrayList<Block>();blocks.add(CHEST.get());CRATES.values().forEach(b->blocks.add(b.get()));return BlockEntityType.Builder.of(ChestEntity::new,blocks.toArray(Block[]::new)).build(null);});
    public static boolean isChest(BlockState state){return state.getBlock() instanceof DungeonChest;}
    public static BlockState chestState(String id){var resource=net.minecraft.resources.ResourceLocation.parse(id);var block=net.minecraft.core.registries.BuiltInRegistries.BLOCK.get(resource);if(!(block instanceof DungeonChest))throw new IllegalArgumentException("Tipo de cofre inválido");return block.defaultBlockState();}
    public static String keyFor(String block){String id=net.minecraft.resources.ResourceLocation.parse(block).getPath();return id.startsWith("locked_")?"zianmanager:"+id.substring(7).replace("_crate","_key"):"";}
    public static void register(IEventBus bus){BLOCKS.register(bus);ITEMS.register(bus);ENTITIES.register(bus);}
    public static final class ChestEntity extends BlockEntity {
        public long animationStarted=-1000;
        public ChestEntity(BlockPos pos,BlockState state){super(CHEST_ENTITY.get(),pos,state);}
        @Override public boolean triggerEvent(int id,int value){if(id==1){animationStarted=level==null?0:level.getGameTime();return true;}return super.triggerEvent(id,value);}
        // No Container/RandomizableContainer inheritance: Lootr and hoppers do not own this inventory.
    }
    public static final class DungeonChest extends BaseEntityBlock {
        public static final MapCodec<DungeonChest> CODEC=simpleCodec(DungeonChest::new);
        public final String model;
        public DungeonChest(Properties p){this(p,"");}
        public DungeonChest(Properties p,String model){super(p);this.model=model;registerDefaultState(stateDefinition.any().setValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.HORIZONTAL_FACING,Direction.NORTH));}
        @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> builder){builder.add(net.minecraft.world.level.block.state.properties.BlockStateProperties.HORIZONTAL_FACING);}
        @Override public BlockState getStateForPlacement(net.minecraft.world.item.context.BlockPlaceContext context){return defaultBlockState().setValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.HORIZONTAL_FACING,context.getHorizontalDirection().getOpposite());}
        @Override protected MapCodec<? extends BaseEntityBlock> codec(){return CODEC;}
        @Override public BlockEntity newBlockEntity(BlockPos pos,BlockState state){return new ChestEntity(pos,state);}
        @Override protected RenderShape getRenderShape(BlockState state){return model.isEmpty()?RenderShape.MODEL:RenderShape.ENTITYBLOCK_ANIMATED;}
        @Override protected net.minecraft.world.phys.shapes.VoxelShape getShape(BlockState state,net.minecraft.world.level.BlockGetter level,BlockPos pos,net.minecraft.world.phys.shapes.CollisionContext context){return Block.box(1,0,1,15,14,15);}
        @Override protected InteractionResult useWithoutItem(BlockState state,Level level,BlockPos pos,Player player,BlockHitResult hit){
            if(player instanceof ServerPlayer target)ManagerRuntime.get().openChest(target,pos);return InteractionResult.sidedSuccess(level.isClientSide);
        }
        @Override public boolean canEntityDestroy(BlockState state,net.minecraft.world.level.BlockGetter level,BlockPos pos,net.minecraft.world.entity.Entity entity){return false;}
    }
}
