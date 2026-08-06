package ru.kasper.woodenwateringcan.block;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public final class WoodenWateringCanBlock extends Block {
    public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;

    private static final VoxelShape WEST_SHAPE = Shapes.or(
            Block.box(4.0, 0.0, 3.0, 14.0, 15.0, 13.0),
            Block.box(0.0, 2.0, 5.0, 6.0, 7.0, 11.0)
    );
    private static final VoxelShape EAST_SHAPE = Shapes.or(
            Block.box(2.0, 0.0, 3.0, 12.0, 15.0, 13.0),
            Block.box(10.0, 2.0, 5.0, 16.0, 7.0, 11.0)
    );
    private static final VoxelShape NORTH_SHAPE = Shapes.or(
            Block.box(3.0, 0.0, 4.0, 13.0, 15.0, 14.0),
            Block.box(5.0, 2.0, 0.0, 11.0, 7.0, 6.0)
    );
    private static final VoxelShape SOUTH_SHAPE = Shapes.or(
            Block.box(3.0, 0.0, 2.0, 13.0, 15.0, 12.0),
            Block.box(5.0, 2.0, 10.0, 11.0, 7.0, 16.0)
    );

    public WoodenWateringCanBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.WEST));
    }

    @Override
    @Nullable
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return shapeFor(state.getValue(FACING));
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return shapeFor(state.getValue(FACING));
    }

    private static VoxelShape shapeFor(Direction direction) {
        return switch (direction) {
            case NORTH -> NORTH_SHAPE;
            case SOUTH -> SOUTH_SHAPE;
            case EAST -> EAST_SHAPE;
            case WEST -> WEST_SHAPE;
            default -> WEST_SHAPE;
        };
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }
}
