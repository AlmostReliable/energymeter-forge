package com.almostreliable.energymeter.block;

import com.almostreliable.energymeter.block.entity.MeterBlockEntity;

import net.minecraft.core.BlockPos;
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
}
