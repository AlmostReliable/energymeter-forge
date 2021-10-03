package dev.rlnt.energymeter.meter;

import static dev.rlnt.energymeter.core.Constants.IO_STATE_ID;
import static dev.rlnt.energymeter.core.Constants.PIPEZ_ID;

import javax.annotation.Nullable;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.vector.Vector3i;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.common.ToolType;
import net.minecraftforge.fml.network.NetworkHooks;

public class MeterBlock extends Block {

    public static final DirectionProperty HORIZONTAL_FACING = BlockStateProperties.HORIZONTAL_FACING;
    /**
     * This BlockState is purely for utility in order to be able to update neighbor blocks.
     * It changes each time the neighbor blocks need updates (e.g. when wires have to connect).
     * This is only used for the updates, not for rendering, textures or other fancy functionality.
     */
    static final BooleanProperty IO = BooleanProperty.create(IO_STATE_ID);

    public MeterBlock() {
        super(Properties.of(Material.METAL).strength(5f).harvestTool(ToolType.PICKAXE).sound(SoundType.METAL));
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(final BlockItemUseContext context) {
        return defaultBlockState()
            .setValue(HORIZONTAL_FACING, context.getHorizontalDirection().getOpposite())
            .setValue(IO, false);
    }

    @Override
    protected void createBlockStateDefinition(final StateContainer.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(HORIZONTAL_FACING);
        builder.add(IO);
    }

    @Override
    public boolean hasTileEntity(final BlockState state) {
        return true;
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(final BlockState state, final IBlockReader level) {
        return new MeterTile(state);
    }

    @SuppressWarnings("deprecation")
    @Override
    public void neighborChanged(
        final BlockState state,
        final World level,
        final BlockPos pos,
        final Block block,
        final BlockPos neighbor,
        final boolean isMoving
    ) {
        super.neighborChanged(state, level, pos, block, neighbor, isMoving);

        // get tile entity from block position
        if (!state.hasTileEntity()) return;
        final TileEntity tile = level.getBlockEntity(pos);
        if (!(tile instanceof MeterTile)) return;

        // ensure valid neighbor
        final BlockState neighborState = level.getBlockState(neighbor);
        final ResourceLocation registryName = neighborState.getBlock().getRegistryName();
        if (
            !neighborState.is(Blocks.AIR) &&
            !neighborState.hasTileEntity() &&
            registryName != null &&
            !registryName.getNamespace().equals(PIPEZ_ID)
        ) return;

        // get direction from neighbor block position
        final Vector3i vector = neighbor.subtract(pos);
        final Direction direction = Direction.fromNormal(vector.getX(), vector.getY(), vector.getZ());
        if (direction == null) return;

        // update the cache from the direction
        ((MeterTile) tile).updateCache(direction);
    }

    @SuppressWarnings("deprecation")
    @Override
    public ActionResultType use(
        final BlockState state,
        final World level,
        final BlockPos pos,
        final PlayerEntity player,
        final Hand hand,
        final BlockRayTraceResult hit
    ) {
        // don't do anything on clientside or if player is shifting
        if (level.isClientSide() || player.isShiftKeyDown()) return ActionResultType.SUCCESS;

        // open the gui for the player who right-clicked the block
        final TileEntity tile = level.getBlockEntity(pos);
        if (tile instanceof INamedContainerProvider && player instanceof ServerPlayerEntity) {
            NetworkHooks.openGui(((ServerPlayerEntity) player), ((INamedContainerProvider) tile), pos);
        }
        return ActionResultType.CONSUME;
    }
}
