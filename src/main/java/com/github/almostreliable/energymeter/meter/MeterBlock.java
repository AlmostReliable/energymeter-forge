package com.github.almostreliable.energymeter.meter;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Plane;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.network.NetworkHooks;

import javax.annotation.Nullable;

import static com.github.almostreliable.energymeter.core.Constants.IO_STATE_ID;
import static com.github.almostreliable.energymeter.core.Constants.PIPEZ_ID;

public class MeterBlock extends Block implements EntityBlock {

    public static final DirectionProperty FACING = BlockStateProperties.FACING;
    public static final DirectionProperty BOTTOM = DirectionProperty.create("bottom", Plane.HORIZONTAL);
    /**
     * This BlockState is purely for utility in order to be able to update neighbor blocks.
     * It changes each time the neighbor blocks need updates (e.g. when wires have to connect).
     * This is only used for the updates, not for rendering, textures or other fancy functionality.
     */
    static final BooleanProperty IO = BooleanProperty.create(IO_STATE_ID);

    public MeterBlock() {
        super(Properties.of(Material.METAL).strength(2f).sound(SoundType.METAL));
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        var facing = context.getNearestLookingDirection().getOpposite();
        var bottom = context.getHorizontalDirection();
        return defaultBlockState()
            .setValue(FACING, facing)
            .setValue(BOTTOM, facing == Direction.DOWN ? bottom : bottom.getOpposite())
            .setValue(IO, false);
    }

    @Override
    protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(FACING);
        builder.add(BOTTOM);
        builder.add(IO);
    }

    @SuppressWarnings("deprecation")
    @Override
    public void neighborChanged(
        BlockState state, Level level, BlockPos pos, Block block, BlockPos neighbor, boolean isMoving
    ) {
        super.neighborChanged(state, level, pos, block, neighbor, isMoving);

        // resolve tile entity from block position
        if (!state.hasBlockEntity()) return;
        if (level.getBlockEntity(pos) instanceof MeterEntity entity) {
            // ensure valid neighbor
            var neighborState = level.getBlockState(neighbor);
            var registryName = neighborState.getBlock().getRegistryName();
            if (!neighborState.isAir() && !neighborState.hasBlockEntity() && registryName != null &&
                !registryName.getNamespace().equals(PIPEZ_ID)) {return;}

            // resolve direction from neighbor block position
            var vector = neighbor.subtract(pos);
            var direction = Direction.fromNormal(vector.getX(), vector.getY(), vector.getZ());
            if (direction == null) return;

            // update the cache from the direction
            entity.updateCache(direction);
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    public InteractionResult use(
        BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit
    ) {
        // don't do anything on clientside or if player is shifting
        if (level.isClientSide() || player.isShiftKeyDown()) return InteractionResult.SUCCESS;

        // open the gui for the player who right-clicked the block
        var tile = level.getBlockEntity(pos);
        if (tile instanceof MenuProvider entity && player instanceof ServerPlayer serverPlayer) {
            NetworkHooks.openGui(serverPlayer, entity, pos);
        }
        return InteractionResult.CONSUME;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new MeterEntity(pos, state);
    }

    @Nullable
    @Override
    public <E extends BlockEntity> BlockEntityTicker<E> getTicker(
        Level level, BlockState state, BlockEntityType<E> entity
    ) {
        if (level.isClientSide) {
            return null;
        }
        return (pLevel, pPos, pState, pEntity) -> {
            if (pEntity instanceof MeterEntity meter) {
                meter.tick();
            }
        };
    }
}
