package net.astellismodding.golfwithmates.block.custom;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class GolfFlagBlock extends HorizontalDirectionalBlock {
    public static final MapCodec<GolfFlagBlock> CODEC = simpleCodec(GolfFlagBlock::new);
    private static final VoxelShape NORTH_BB = Block.box(7, 0, 2, 9,16,4);
    private static final VoxelShape SOUTH_BB = Block.box(7, 0, 12, 9,16,14);
    private static final VoxelShape WEST_BB = Block.box(2, 0, 7, 4,16,9);
    private static final VoxelShape EAST_BB = Block.box(12, 0, 7, 14,16,9);

    public GolfFlagBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        switch ((Direction) state.getValue(FACING)) {
            case NORTH:
            default:
                return NORTH_BB;
            case SOUTH:
                return SOUTH_BB;
            case WEST:
                return WEST_BB;
            case EAST:
                return EAST_BB;
        }
    }

    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection());
    }

    @Override
    protected MapCodec<? extends HorizontalDirectionalBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }
}
