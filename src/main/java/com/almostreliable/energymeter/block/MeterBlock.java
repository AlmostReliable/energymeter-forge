package com.almostreliable.energymeter.block;

import com.almostreliable.energymeter.block.entity.MeterBlockEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import org.jetbrains.annotations.Nullable;

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
    protected BlockState updateShape(
        BlockState state,
        Direction directionToNeighbor,
        BlockState neighborState,
        LevelAccessor level,
        BlockPos pos,
        BlockPos neighborPos
    ) {
        if (level instanceof ServerLevel serverLevel && serverLevel.getBlockEntity(pos) instanceof MeterBlockEntity meterBlockEntity) {
            meterBlockEntity.onNeighborBlockChange(directionToNeighbor);
        }

        return super.updateShape(state, directionToNeighbor, neighborState, level, pos, neighborPos);
    }
}
