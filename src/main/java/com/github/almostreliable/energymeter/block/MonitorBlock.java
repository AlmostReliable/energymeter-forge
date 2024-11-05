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
import com.github.almostreliable.energymeter.block.multiblock.MultiblockData;
import com.github.almostreliable.energymeter.block.multiblock.MultiblockType;
import com.github.almostreliable.energymeter.block.multiblock.MultiblockTypeProperty;
import com.github.almostreliable.energymeter.block.multiblock.OptionalDirection;
import com.github.almostreliable.energymeter.block.multiblock.OptionalDirectionProperty;
import com.github.almostreliable.energymeter.core.Config;

import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class MonitorBlock extends FacingEntityBlock {

    public static final BooleanProperty CONTROLLER = BooleanProperty.create("controller");
    public static final OptionalDirectionProperty HORIZONTAL = OptionalDirectionProperty.HORIZONTAL;
    public static final OptionalDirectionProperty VERTICAL = OptionalDirectionProperty.VERTICAL;
    public static final MultiblockTypeProperty TYPE = MultiblockTypeProperty.INSTANCE;

    public MonitorBlock(Properties properties) {
        super(properties);
        registerDefaultState(
            defaultBlockState()
                .setValue(CONTROLLER, false)
                .setValue(HORIZONTAL, OptionalDirection.NONE)
                .setValue(VERTICAL, OptionalDirection.NONE)
                .setValue(TYPE, MultiblockType.NORMAL)
        );
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(CONTROLLER, HORIZONTAL, VERTICAL, TYPE);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        if (state.getValue(CONTROLLER).equals(Boolean.TRUE)) {
            return new MonitorBlockEntity(pos, state);
        }

        return null;
    }

    @Nullable
    @Override
    protected MenuProvider getMenuProvider(BlockState state, Level level, BlockPos pos) {
        if (isUnbound(state)) return null;
        BlockPos controllerPos = findControllerPos(level, pos, state);
        return super.getMenuProvider(state, level, controllerPos == null ? pos : controllerPos);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (!level.isClientSide && !player.isShiftKeyDown()) {
            if (isUnbound(state)) {
                BlockPos controllerPos = findAndSetController(level, pos, state);
                formMonitor(level, controllerPos, player);
                return InteractionResult.SUCCESS_NO_ITEM_USED;
            }
        }

        return super.useWithoutItem(state, level, pos, player, hitResult);
    }

    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (!level.isClientSide && !isUnbound(state)) {
            BlockPos controllerPos = findControllerPos(level, pos, state);
            if (controllerPos != null && level.getBlockEntity(controllerPos) instanceof MonitorBlockEntity controllerBlockEntity) {
                destroyMonitor(level, controllerBlockEntity, player);
            }
        }

        return super.playerWillDestroy(level, pos, state, player);
    }

    private static boolean isUnbound(BlockState state) {
        return state.getValue(CONTROLLER).equals(Boolean.FALSE) &&
            state.getValue(HORIZONTAL).isNone() &&
            state.getValue(VERTICAL).isNone();
    }

    private static boolean isBindableMonitorBlock(BlockState state, Direction facingDir, Direction bottomDir) {
        return state.getBlock() instanceof MonitorBlock &&
            isUnbound(state) &&
            state.getValue(FACING) == facingDir &&
            state.getValue(BOTTOM) == bottomDir;
    }

    private static BlockPos findAndSetController(Level level, BlockPos pos, BlockState state) {
        Direction facingDir = getFacingDir(state);
        Direction bottomDir = getBottomDir(state);
        Direction leftDir = getLeftDir(state);
        int maxWidth = Config.COMMON.maxWidth.getAsInt();
        int maxHeight = Config.COMMON.maxHeight.getAsInt();

        BlockPos controllerPos = findFurthestMonitorPosInDir(level, pos, leftDir, facingDir, bottomDir, maxWidth).pos;
        controllerPos = findFurthestMonitorPosInDir(level, controllerPos, bottomDir, facingDir, bottomDir, maxHeight).pos;

        BlockState controllerState = level.getBlockState(controllerPos);
        level.setBlock(controllerPos, controllerState.setValue(CONTROLLER, true), 1 | 2);

        return controllerPos;
    }

    private static MonitorSearchResult findFurthestMonitorPosInDir(
        Level level, BlockPos startPos, Direction direction, Direction facing, Direction bottom, int max
    ) {
        int distance = 0;
        BlockPos resultPos = startPos;
        BlockPos travelPos = startPos.relative(direction);

        while (distance < max && isBindableMonitorBlock(level.getBlockState(travelPos), facing, bottom)) {
            distance++;
            resultPos = travelPos;
            travelPos = travelPos.relative(direction);
        }

        return new MonitorSearchResult(distance, resultPos);
    }

    private static void formMonitor(Level level, BlockPos controllerPos, Player player) {
        BlockState controllerState = level.getBlockState(controllerPos);
        Direction facingDir = getFacingDir(controllerState);
        Direction bottomDir = getBottomDir(controllerState);
        Direction topDir = bottomDir.getOpposite();
        Direction rightDir = getLeftDir(controllerState).getOpposite();
        int maxWidth = Config.COMMON.maxWidth.getAsInt();
        int maxHeight = Config.COMMON.maxHeight.getAsInt();

        int top = findFurthestMonitorPosInDir(level, controllerPos, topDir, facingDir, bottomDir, maxHeight).distance;
        int right = findFurthestMonitorPosInDir(level, controllerPos, rightDir, facingDir, bottomDir, maxWidth).distance;

        if (top == 0 && right == 0) {
            player.displayClientMessage(Component.literal("single monitor formed").withStyle(ChatFormatting.DARK_GREEN), true);
            return;
        }

        Map<BlockPos, BlockState> monitorStates = new HashMap<>();

        for (int y = 0; y <= top; y++) {
            for (int x = 0; x <= right; x++) {
                BlockPos cursor = controllerPos.relative(topDir, y).relative(rightDir, x);

                BlockState monitorState = level.getBlockState(cursor);
                if (!cursor.equals(controllerPos) && !isBindableMonitorBlock(monitorState, facingDir, bottomDir)) {
                    player.displayClientMessage(Component.literal("invalid multiblock").withStyle(ChatFormatting.DARK_RED), true);
                    return;
                }

                monitorState = setControllerOffsetProps(monitorState, cursor, controllerPos);
                monitorState = setMultiblockTypeProps(monitorState, x, y, right, top);
                monitorStates.put(cursor.immutable(), monitorState);
            }
        }

        if (level.getBlockEntity(controllerPos) instanceof MonitorBlockEntity monitorBlockEntity) {
            monitorBlockEntity.setMultiblockData(
                new MultiblockData(controllerPos, controllerPos.relative(topDir, top).relative(rightDir, right))
            );

            for (var entry : monitorStates.entrySet()) {
                level.setBlock(entry.getKey(), entry.getValue(), 1 | 2);
            }

            player.displayClientMessage(Component.literal("multiblock formed").withStyle(ChatFormatting.DARK_GREEN), true);
            return;
        }

        player.displayClientMessage(Component.literal("data writing failed").withStyle(ChatFormatting.DARK_RED), true);
    }

    private void destroyMonitor(Level level, MonitorBlockEntity controller, Player player) {
        MultiblockData data = controller.getMultiblockData();
        if (data == null) return;

        BlockPos bottomLeft = data.bottomLeft();
        BlockPos topRight = data.topRight();
        Direction facing = controller.getBlockState().getValue(FACING);
        Direction bottom = controller.getBlockState().getValue(BOTTOM);

        for (var pos : BlockPos.betweenClosed(bottomLeft, topRight)) {
            BlockState state = level.getBlockState(pos);
            if (state.getBlock() instanceof MonitorBlock) {
                level.setBlock(pos, defaultBlockState().setValue(FACING, facing).setValue(BOTTOM, bottom), 1 | 2);
            }
        }

        player.displayClientMessage(Component.literal("multiblock destroyed").withStyle(ChatFormatting.DARK_GREEN), true);
    }

    @Nullable
    private static BlockPos findControllerPos(Level level, BlockPos pos, BlockState state) {
        if (!(state.getBlock() instanceof MonitorBlock)) {
            return null;
        }

        OptionalDirection horizontal = state.getValue(HORIZONTAL);
        OptionalDirection vertical = state.getValue(VERTICAL);
        boolean isController = state.getValue(CONTROLLER);

        if (isController) return pos;

        var nextPos = pos.mutable();
        horizontal.relative(nextPos);
        vertical.relative(nextPos);
        BlockState nextState = level.getBlockState(nextPos);

        return findControllerPos(level, nextPos, nextState);
    }

    private static BlockState setMultiblockTypeProps(BlockState state, int x, int y, int right, int top) {
        if (x == 0 && y == 0) {
            if (right == 0) return state.setValue(TYPE, MultiblockType.CORNER_D);
            if (top == 0) return state.setValue(TYPE, MultiblockType.CORNER_L);
            return state.setValue(TYPE, MultiblockType.CORNER_DL);
        }
        if (x == right && y == 0) {
            if (top == 0) return state.setValue(TYPE, MultiblockType.CORNER_R);
            return state.setValue(TYPE, MultiblockType.CORNER_DR);
        }
        if (x == 0 && y == top) {
            if (right == 0) return state.setValue(TYPE, MultiblockType.CORNER_U);
            return state.setValue(TYPE, MultiblockType.CORNER_UL);
        }
        if (x == right && y == top) {
            return state.setValue(TYPE, MultiblockType.CORNER_UR);
        }
        if (x > 0 && x < right && y > 0 && y < top) {
            return state.setValue(TYPE, MultiblockType.MIDDLE);
        }
        if (x == 0 && y > 0 && y < top) {
            if (right == 0) return state.setValue(TYPE, MultiblockType.VERTICAL);
            return state.setValue(TYPE, MultiblockType.SIDE_L);
        }
        if (x == right && y > 0 && y < top) {
            return state.setValue(TYPE, MultiblockType.SIDE_R);
        }
        if (x > 0 && x < right && y == 0) {
            if (top == 0) return state.setValue(TYPE, MultiblockType.HORIZONTAL);
            return state.setValue(TYPE, MultiblockType.SIDE_D);
        }
        if (x > 0 && x < right && y == top) {
            return state.setValue(TYPE, MultiblockType.SIDE_U);
        }
        return state.setValue(TYPE, MultiblockType.NORMAL);
    }

    private static BlockState setControllerOffsetProps(BlockState state, BlockPos pos, BlockPos controllerPos) {
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

    private record MonitorSearchResult(int distance, BlockPos pos) {}
}
