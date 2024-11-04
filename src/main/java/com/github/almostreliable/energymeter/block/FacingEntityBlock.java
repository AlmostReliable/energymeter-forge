package com.github.almostreliable.energymeter.block;

import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;

import org.jetbrains.annotations.Nullable;

public abstract class FacingEntityBlock extends Block implements EntityBlock {

    public static final DirectionProperty FACING = BlockStateProperties.FACING;
    public static final DirectionProperty BOTTOM = DirectionProperty.create("bottom");

    FacingEntityBlock(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState().setValue(FACING, Direction.NORTH).setValue(BOTTOM, Direction.DOWN));
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction facing = context.getNearestLookingDirection().getOpposite();
        Direction bottom = switch (facing) {
            case UP -> context.getHorizontalDirection().getOpposite();
            case DOWN -> context.getHorizontalDirection();
            default -> Direction.DOWN;
        };
        return defaultBlockState().setValue(FACING, facing).setValue(BOTTOM, bottom);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(FACING, BOTTOM);
    }

    public static Direction getFacingDir(BlockState state) {
        return state.getValue(FACING);
    }

    public static Direction getBottomDir(BlockState state) {
        return state.getValue(BOTTOM);
    }

    public static Direction getLeftDir(BlockState state) {
        Direction facing = getFacingDir(state);
        Direction bottom = getBottomDir(state);
        return switch (facing) {
            case UP -> bottom.getClockWise();
            case DOWN -> bottom.getCounterClockWise();
            default -> facing.getClockWise();
        };
    }
}
