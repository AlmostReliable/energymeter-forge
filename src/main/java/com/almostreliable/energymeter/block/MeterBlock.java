package com.almostreliable.energymeter.block;

import com.almostreliable.energymeter.block.entity.MeterBlockEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
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
    protected void neighborChanged(
        BlockState state, Level level, BlockPos pos, Block neighborBlock, BlockPos neighborPos, boolean movedByPiston
    ) {
        super.neighborChanged(state, level, pos, neighborBlock, neighborPos, movedByPiston);
        if (!(level instanceof ServerLevel serverLevel)) return;

        var blockEntity = serverLevel.getBlockEntity(pos);
        var newState = serverLevel.getBlockState(neighborPos);
        if (blockEntity instanceof MeterBlockEntity meterBlockEntity && neighborBlock != newState.getBlock()) {
            var vector = neighborPos.subtract(pos);
            var direction = Direction.fromDelta(vector.getX(), vector.getY(), vector.getZ());
            if (direction == null) return;
            meterBlockEntity.onNeighborBlockChange(direction);
        }
    }
}
