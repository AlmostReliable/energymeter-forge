package com.almostreliable.energymeter.block;

import com.almostreliable.energymeter.block.entity.TickableBlock;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;

import org.jetbrains.annotations.Nullable;

public abstract class FacingEntityBlock extends TickableBlock {

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

    @Nullable
    @Override
    protected MenuProvider getMenuProvider(BlockState state, Level level, BlockPos pos) {
        if (level.getBlockEntity(pos) instanceof MenuProvider menuProvider) {
            return menuProvider;
        }

        return super.getMenuProvider(state, level, pos);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (player instanceof ServerPlayer serverPlayer && !serverPlayer.isShiftKeyDown()) {
            MenuProvider menuProvider = getMenuProvider(state, level, pos);
            if (menuProvider != null) {
                serverPlayer.openMenu(menuProvider, pos);
            }
        }

        return InteractionResult.sidedSuccess(level.isClientSide);
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
