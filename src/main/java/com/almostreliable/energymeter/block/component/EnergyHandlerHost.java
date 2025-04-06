package com.almostreliable.energymeter.block.component;

import com.almostreliable.energymeter.util.TypeEnums;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

import org.jetbrains.annotations.Nullable;

public interface EnergyHandlerHost {

    @Nullable
    Level getLevel();

    BlockPos getBlockPos();

    boolean isRemoved();

    IoConfig getIoConfig();

    TypeEnums.TransferMode getTransferMode();

    int getTransferLimit();
}
