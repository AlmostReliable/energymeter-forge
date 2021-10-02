package dev.rlnt.energymeter.meter;

import static dev.rlnt.energymeter.core.Constants.IO_STATE_ID;
import static dev.rlnt.energymeter.core.Constants.PIPEZ_ID;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.resources.ResourceLocation;
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
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.fmllegacy.network.NetworkHooks;

public class MeterBlock extends Block implements EntityBlock {

    public static final DirectionProperty HORIZONTAL_FACING = BlockStateProperties.HORIZONTAL_FACING;
    /**
     * This BlockState is purely for utility in order to be able to update neighbor blocks.
     * It changes each time the neighbor blocks need updates (e.g. when wires have to connect).
     * This is only used for the updates, not for rendering, textures or other fancy functionality.
     */
    static final BooleanProperty IO = BooleanProperty.create(IO_STATE_ID);

    public MeterBlock() {
        super(Properties.of(Material.METAL).strength(5f).requiresCorrectToolForDrops().sound(SoundType.METAL));
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(final BlockPlaceContext context) {
        return defaultBlockState()
            .setValue(HORIZONTAL_FACING, context.getHorizontalDirection().getOpposite())
            .setValue(IO, false);
    }

    @Override
    protected void createBlockStateDefinition(final StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(HORIZONTAL_FACING);
        builder.add(IO);
    }

    @SuppressWarnings("deprecation")
    @Override
    public void neighborChanged(
        final BlockState state,
        final Level level,
        final BlockPos pos,
        final Block block,
        final BlockPos neighbor,
        final boolean isMoving
    ) {
        super.neighborChanged(state, level, pos, block, neighbor, isMoving);

        // get tile entity from block position
        if (!state.hasBlockEntity()) return;
        if (level.getBlockEntity(pos) instanceof MeterEntity tile) {
            // ensure valid neighbor
            final BlockState neighborState = level.getBlockState(neighbor);
            final ResourceLocation registryName = neighborState.getBlock().getRegistryName();
            if (
                !neighborState.isAir() &&
                !neighborState.hasBlockEntity() &&
                registryName != null &&
                !registryName.getNamespace().equals(PIPEZ_ID)
            ) return;

            // get direction from neighbor block position
            final Vec3i vector = neighbor.subtract(pos);
            final Direction direction = Direction.fromNormal(vector.getX(), vector.getY(), vector.getZ());
            if (direction == null) return;

            // update the cache from the direction
            tile.updateCache(direction);
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    public InteractionResult use(
        final BlockState state,
        final Level level,
        final BlockPos pos,
        final Player player,
        final InteractionHand hand,
        final BlockHitResult hit
    ) {
        // don't do anything on clientside or if player is shifting
        if (level.isClientSide() || player.isShiftKeyDown()) return InteractionResult.SUCCESS;

        // open the gui for the player who right-clicked the block
        final BlockEntity tile = level.getBlockEntity(pos);
        if (tile instanceof MenuProvider entity && player instanceof ServerPlayer serverPlayer) {
            NetworkHooks.openGui(serverPlayer, entity, pos);
        }
        return InteractionResult.CONSUME;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(final BlockPos pos, final BlockState state) {
        return new MeterEntity(pos, state);
    }

    @Nullable
    @Override
    public <E extends BlockEntity> BlockEntityTicker<E> getTicker(
        final Level level,
        final BlockState state,
        final BlockEntityType<E> entity
    ) {
        if (level.isClientSide) {
            return null;
        } else {
            return (Level pLevel, BlockPos pPos, BlockState pState, E pEntity) -> {
                if (pEntity instanceof MeterEntity meter) {
                    meter.tick();
                }
            };
        }
    }
}
