package com.github.almostreliable.energymeter.block.multiblock;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;

import com.github.almostreliable.energymeter.core.Constants;

public record MultiblockData(BlockPos bottomLeft, BlockPos topRight) {

    public CompoundTag serialize() {
        CompoundTag tag = new CompoundTag();
        tag.put(Constants.BOTTOM_LEFT_ID, NbtUtils.writeBlockPos(bottomLeft));
        tag.put(Constants.TOP_RIGHT_ID, NbtUtils.writeBlockPos(topRight));
        return tag;
    }

    public static MultiblockData deserialize(CompoundTag tag) {
        BlockPos bottomLeft = NbtUtils.readBlockPos(tag, Constants.BOTTOM_LEFT_ID).orElse(BlockPos.ZERO);
        BlockPos topRight = NbtUtils.readBlockPos(tag, Constants.TOP_RIGHT_ID).orElse(BlockPos.ZERO);
        return new MultiblockData(bottomLeft, topRight);
    }
}
