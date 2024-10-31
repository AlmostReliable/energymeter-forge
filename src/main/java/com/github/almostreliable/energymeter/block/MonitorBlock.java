package com.github.almostreliable.energymeter.block;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;

import com.github.almostreliable.energymeter.block.entity.MonitorBlockEntity;
import com.github.almostreliable.energymeter.block.property.OptionalDirection;
import com.github.almostreliable.energymeter.block.property.OptionalDirectionProperty;

import org.jetbrains.annotations.Nullable;

public class MonitorBlock extends FacingEntityBlock {

    public static final BooleanProperty CONTROLLER = BooleanProperty.create("controller");
    public static final OptionalDirectionProperty HORIZONTAL = OptionalDirectionProperty.HORIZONTAL;
    public static final OptionalDirectionProperty VERTICAL = OptionalDirectionProperty.VERTICAL;

    public MonitorBlock(Properties properties) {
        super(properties);
        registerDefaultState(
            defaultBlockState()
                .setValue(CONTROLLER, false)
                .setValue(HORIZONTAL, OptionalDirection.NONE)
                .setValue(VERTICAL, OptionalDirection.NONE)
        );
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(CONTROLLER, HORIZONTAL, VERTICAL);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        if (Boolean.TRUE.equals(state.getValue(CONTROLLER))) {
            return new MonitorBlockEntity(pos, state);
        }
        return null;
    }

    @Nullable
    @Override
    protected MenuProvider getMenuProvider(BlockState state, Level level, BlockPos pos) {
        if (isUnbound(state)) {
            return null;
        }

        BlockPos controllerPos = findControllerPos(level, pos, state);
        if (controllerPos != null && level.getBlockEntity(controllerPos) instanceof MenuProvider menuProvider) {
            return menuProvider;
        }

        return null;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (!level.isClientSide && !player.isShiftKeyDown()) {
            if (isUnbound(state) && !formMonitor(state, level, pos, player)) {
                return InteractionResult.FAIL;
            }

            player.openMenu(getMenuProvider(state, level, pos));
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    private static boolean isUnbound(BlockState state) {
        return Boolean.FALSE.equals(state.getValue(CONTROLLER)) &&
            state.getValue(HORIZONTAL).isNone() &&
            state.getValue(VERTICAL).isNone();
    }

    private static boolean isBindableMonitorBlock(BlockState state, Direction facingDir, Direction bottomDir) {
        return state.getBlock() instanceof MonitorBlock &&
            isUnbound(state) &&
            state.getValue(FACING) == facingDir &&
            state.getValue(BOTTOM) == bottomDir;
    }

    private static int countMonitorsInDir(Level level, BlockPos pos, Direction direction, Direction facingDir, Direction bottomDir) {
        int count = 0;
        BlockPos nextPos = pos.relative(direction);
        BlockState nextState = level.getBlockState(nextPos);

        while (isBindableMonitorBlock(nextState, facingDir, bottomDir)) {
            count++;
            nextPos = nextPos.relative(direction);
            nextState = level.getBlockState(nextPos);
        }

        return count;
    }

    private static boolean formMonitor(BlockState state, Level level, BlockPos pos, Player player) {
        Direction facingDir = getFacingDir(state);
        Direction bottomDir = getBottomDir(state);
        Direction leftDir = getLeftDir(state);

        int top = countMonitorsInDir(level, pos, bottomDir.getOpposite(), facingDir, bottomDir);
        int bottom = countMonitorsInDir(level, pos, bottomDir, facingDir, bottomDir);
        int left = countMonitorsInDir(level, pos, leftDir, facingDir, bottomDir);
        int right = countMonitorsInDir(level, pos, leftDir.getOpposite(), facingDir, bottomDir);

        if (top == 0 && bottom == 0 && left == 0 && right == 0) {
            level.setBlock(pos, state.setValue(CONTROLLER, true), 3);
            return true;
        }

        BlockPos bottomLeftPos = pos.relative(bottomDir, bottom).relative(leftDir, left);
        BlockPos topRightPos = pos.relative(bottomDir.getOpposite(), top).relative(leftDir.getOpposite(), right);

        for (BlockPos monitorPos : BlockPos.betweenClosed(bottomLeftPos, topRightPos)) {
            BlockState monitorState = level.getBlockState(monitorPos);
            if (!isBindableMonitorBlock(monitorState, facingDir, bottomDir)) {
                player.displayClientMessage(Component.literal("invalid multiblock").withStyle(ChatFormatting.DARK_RED), true);
                return false;
            }

            BlockState newMonitorState = setOffsetState(monitorState, monitorPos, bottomLeftPos);
            level.setBlock(monitorPos, newMonitorState, 3);
        }

        BlockState bottomLeft = level.getBlockState(bottomLeftPos);
        level.setBlock(bottomLeftPos, bottomLeft.setValue(CONTROLLER, true), 3);
        player.displayClientMessage(Component.literal("multiblock formed").withStyle(ChatFormatting.DARK_GREEN), true);

        return true;
    }

    @Nullable
    private static BlockPos findControllerPos(Level level, BlockPos pos, BlockState state) {
        if (!(state.getBlock() instanceof MonitorBlock)) {
            return null;
        }

        OptionalDirection horizontal = state.getValue(HORIZONTAL);
        OptionalDirection vertical = state.getValue(VERTICAL);
        boolean isController = state.getValue(CONTROLLER);

        if (isController) {
            if (!horizontal.isNone() || !vertical.isNone()) {
                // error
                return null;
            }

            return pos;
        }

        var nextPos = pos.mutable();
        horizontal.relative(nextPos);
        vertical.relative(nextPos);
        BlockState nextState = level.getBlockState(nextPos);

        return findControllerPos(level, nextPos, nextState);
    }

    private static BlockState setOffsetState(BlockState state, BlockPos pos, BlockPos controllerPos) {
        return state
            .setValue(HORIZONTAL, getHorizontalOffset(pos, controllerPos))
            .setValue(VERTICAL, getVerticalOffset(pos, controllerPos));
    }

    private static OptionalDirection getHorizontalOffset(BlockPos pos, BlockPos controllerPos) {
        if (controllerPos.getX() == pos.getX() && controllerPos.getZ() == pos.getZ()) {
            return OptionalDirection.NONE;
        }
        if (controllerPos.getZ() < pos.getZ()) {
            return OptionalDirection.NORTH;
        }
        if (controllerPos.getZ() > pos.getZ()) {
            return OptionalDirection.SOUTH;
        }
        if (controllerPos.getX() < pos.getX()) {
            return OptionalDirection.WEST;
        }
        if (controllerPos.getX() > pos.getX()) {
            return OptionalDirection.EAST;
        }
        return OptionalDirection.NONE;
    }

    private static OptionalDirection getVerticalOffset(BlockPos hullPos, BlockPos controllerPos) {
        if (controllerPos.getY() == hullPos.getY()) {
            return OptionalDirection.NONE;
        }
        return controllerPos.getY() < hullPos.getY() ? OptionalDirection.DOWN : OptionalDirection.UP;
    }
}
