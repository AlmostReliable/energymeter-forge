package com.almostreliable.energymeter.block;

import com.almostreliable.energymeter.block.entity.MeterBlockEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.redstone.Orientation;

import org.jspecify.annotations.Nullable;

public class MeterBlock extends FacingEntityBlock {

    public MeterBlock(Properties properties) {
        super(properties);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new MeterBlockEntity(pos, state);
    }

    @Override
    protected void neighborChanged(
        BlockState state, Level level, BlockPos pos, Block neighborBlock, @Nullable Orientation orientation, boolean movedByPiston
    ) {
        super.neighborChanged(state, level, pos, neighborBlock, orientation, movedByPiston);
        if (!(level instanceof ServerLevel serverLevel)) return;

        if (serverLevel.getBlockEntity(pos) instanceof MeterBlockEntity meterBlockEntity) {
            for (Direction direction : Direction.values()) {
                meterBlockEntity.onNeighborBlockChange(direction);
            }
        }
    }
}
