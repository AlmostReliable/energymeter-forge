package dev.rlnt.energymeter.component;

import dev.rlnt.energymeter.util.TypeEnums.IO_SETTING;
import dev.rlnt.energymeter.util.TypeEnums.MODE;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public final class SidedEnergyStorage implements IEnergyStorage {

    private final IMeter parent;
    private final Direction side;

    private SidedEnergyStorage(IMeter parent, Direction side) {
        this.parent = parent;
        this.side = side;
    }

    /**
     * Creates a new instance for each {@link Direction}.
     * Each of them will have the passed in parent and the direction linked to it.
     *
     * @param parent the parent {@link TileEntity} which implements {@link IMeter}
     * @return a {@link List} of all created instances
     */
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
