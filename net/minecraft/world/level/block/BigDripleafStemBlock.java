package net.minecraft.world.level.block;

import java.util.Optional;
import net.minecraft.BlockUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class BigDripleafStemBlock extends HorizontalDirectionalBlock implements BonemealableBlock, SimpleWaterloggedBlock {
   private static final BooleanProperty WATERLOGGED;
   private static final int STEM_WIDTH = 6;
   protected static final VoxelShape NORTH_SHAPE;
   protected static final VoxelShape SOUTH_SHAPE;
   protected static final VoxelShape EAST_SHAPE;
   protected static final VoxelShape WEST_SHAPE;

   protected BigDripleafStemBlock(BlockBehaviour.Properties var1) {
      super(var1);
      this.registerDefaultState((BlockState)((BlockState)((BlockState)this.stateDefinition.any()).setValue(WATERLOGGED, false)).setValue(FACING, Direction.NORTH));
   }

   public VoxelShape getShape(BlockState var1, BlockGetter var2, BlockPos var3, CollisionContext var4) {
      switch((Direction)var1.getValue(FACING)) {
      case SOUTH:
         return SOUTH_SHAPE;
      case NORTH:
      default:
         return NORTH_SHAPE;
      case WEST:
         return WEST_SHAPE;
      case EAST:
         return EAST_SHAPE;
      }
   }

   protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> var1) {
      var1.add(WATERLOGGED, FACING);
   }

   public FluidState getFluidState(BlockState var1) {
      return (Boolean)var1.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(var1);
   }

   public boolean canSurvive(BlockState var1, LevelReader var2, BlockPos var3) {
      BlockPos var4 = var3.below();
      BlockState var5 = var2.getBlockState(var4);
      BlockState var6 = var2.getBlockState(var3.above());
      return (var5.is(this) || var5.is(BlockTags.BIG_DRIPLEAF_PLACEABLE)) && (var6.is(this) || var6.is(Blocks.BIG_DRIPLEAF));
   }

   protected static boolean place(LevelAccessor var0, BlockPos var1, FluidState var2, Direction var3) {
      BlockState var4 = (BlockState)((BlockState)Blocks.BIG_DRIPLEAF_STEM.defaultBlockState().setValue(WATERLOGGED, var2.isSourceOfType(Fluids.WATER))).setValue(FACING, var3);
      return var0.setBlock(var1, var4, 3);
   }

   public BlockState updateShape(BlockState var1, Direction var2, BlockState var3, LevelAccessor var4, BlockPos var5, BlockPos var6) {
      if ((var2 == Direction.DOWN || var2 == Direction.UP) && !var1.canSurvive(var4, var5)) {
         var4.scheduleTick(var5, (Block)this, 1);
      }

      if ((Boolean)var1.getValue(WATERLOGGED)) {
         var4.scheduleTick(var5, (Fluid)Fluids.WATER, Fluids.WATER.getTickDelay(var4));
      }

      return super.updateShape(var1, var2, var3, var4, var5, var6);
   }

   public void tick(BlockState var1, ServerLevel var2, BlockPos var3, RandomSource var4) {
      if (!var1.canSurvive(var2, var3)) {
         var2.destroyBlock(var3, true);
      }

   }

   public boolean isValidBonemealTarget(BlockGetter var1, BlockPos var2, BlockState var3, boolean var4) {
      Optional var5 = BlockUtil.getTopConnectedBlock(var1, var2, var3.getBlock(), Direction.UP, Blocks.BIG_DRIPLEAF);
      if (!var5.isPresent()) {
         return false;
      } else {
         BlockPos var6 = ((BlockPos)var5.get()).above();
         BlockState var7 = var1.getBlockState(var6);
         return BigDripleafBlock.canPlaceAt(var1, var6, var7);
      }
   }

   public boolean isBonemealSuccess(Level var1, RandomSource var2, BlockPos var3, BlockState var4) {
      return true;
   }

   public void performBonemeal(ServerLevel var1, RandomSource var2, BlockPos var3, BlockState var4) {
      Optional var5 = BlockUtil.getTopConnectedBlock(var1, var3, var4.getBlock(), Direction.UP, Blocks.BIG_DRIPLEAF);
      if (var5.isPresent()) {
         BlockPos var6 = (BlockPos)var5.get();
         BlockPos var7 = var6.above();
         Direction var8 = (Direction)var4.getValue(FACING);
         place(var1, var6, var1.getFluidState(var6), var8);
         BigDripleafBlock.place(var1, var7, var1.getFluidState(var7), var8);
      }
   }

   public ItemStack getCloneItemStack(BlockGetter var1, BlockPos var2, BlockState var3) {
      return new ItemStack(Blocks.BIG_DRIPLEAF);
   }

   static {
      WATERLOGGED = BlockStateProperties.WATERLOGGED;
      NORTH_SHAPE = Block.box(5.0D, 0.0D, 9.0D, 11.0D, 16.0D, 15.0D);
      SOUTH_SHAPE = Block.box(5.0D, 0.0D, 1.0D, 11.0D, 16.0D, 7.0D);
      EAST_SHAPE = Block.box(1.0D, 0.0D, 5.0D, 7.0D, 16.0D, 11.0D);
      WEST_SHAPE = Block.box(9.0D, 0.0D, 5.0D, 15.0D, 16.0D, 11.0D);
   }
}
