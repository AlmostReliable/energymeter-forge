package com.github.almostreliable.energymeter.component;

import com.github.almostreliable.energymeter.meter.MeterTile;
import com.github.almostreliable.energymeter.util.TypeEnums.IO_SETTING;
import com.github.almostreliable.energymeter.util.TypeEnums.MODE;
import net.minecraft.util.Direction;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public final class SidedEnergyStorage implements IEnergyStorage {

    private final MeterTile parent;
    private final Direction side;

    private SidedEnergyStorage(MeterTile parent, Direction side) {
        this.parent = parent;
        this.side = side;
    }

    /**
     * Creates a new instance for each direction.
     *
     * @param parent the parent tile
     * @return a list of all created instances
     */
    public static List<LazyOptional<SidedEnergyStorage>> create(MeterTile parent) {
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
