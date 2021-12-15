package com.github.almostreliable.energymeter.component;

import com.github.almostreliable.energymeter.util.TypeEnums.IO_SETTING;
import com.github.almostreliable.energymeter.util.TypeEnums.MODE;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public record SidedEnergyStorage(IMeter parent, Direction side) implements IEnergyStorage {

    /**
     * Creates a new instance for each {@link Direction}.
     * Each of them will have the passed in parent and the direction linked to it.
     *
     * @param parent the parent {@link BlockEntity} which implements {@link IMeter}
     * @return a {@link List} of all created instances
     */
    @SuppressWarnings("StaticMethodOnlyUsedInOneClass")
    public static List<LazyOptional<SidedEnergyStorage>> create(IMeter parent) {
        return Arrays
            .stream(Direction.values())
            .map(direction -> LazyOptional.of(() -> new SidedEnergyStorage(parent, direction)))
            .collect(Collectors.toList());
    }

    @Override
    public int receiveEnergy(int maxReceive, boolean simulate) {
        if (!canReceive()) return 0;
        return parent.receiveEnergy(maxReceive, simulate);
    }

    @Override
    public int extractEnergy(int maxExtract, boolean simulate) {
        return 0;
    }

    @Override
    public int getEnergyStored() {
        return 0;
    }

    @Override
    public int getMaxEnergyStored() {
        return 0;
    }

    @Override
    public boolean canExtract() {
        return parent.getSideConfig().get(side) == IO_SETTING.OUT && parent.getSideConfig().hasInput();
    }

    @Override
    public boolean canReceive() {
        if (parent.getMode() == MODE.CONSUMER) {
            return parent.getSideConfig().get(side) == IO_SETTING.IN;
        }
        return parent.getSideConfig().get(side) == IO_SETTING.IN && parent.getSideConfig().hasOutput();
    }
}
