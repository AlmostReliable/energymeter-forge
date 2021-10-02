package dev.rlnt.energymeter.energy;

import dev.rlnt.energymeter.util.TypeEnums.IO_SETTING;
import dev.rlnt.energymeter.util.TypeEnums.MODE;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;

public record SidedEnergyStorage(ISidedEnergy parent, Direction side) implements IEnergyStorage {
    /**
     * Creates a new {@link SidedEnergyStorage} for each {@link Direction}.
     * Each of them will have the passed in parent and the direction linked to it.
     *
     * @param parent the parent {@link BlockEntity} which implements {@link ISidedEnergy}
     * @return a {@link List} of all created {@link SidedEnergyStorage}s
     */
    public static List<LazyOptional<SidedEnergyStorage>> create(final ISidedEnergy parent) {
        final List<LazyOptional<SidedEnergyStorage>> res = new ArrayList<>();
        for (final Direction direction : Direction.values()) {
            res.add(LazyOptional.of(() -> new SidedEnergyStorage(parent, direction)));
        }
        return res;
    }

    @Override
    public int receiveEnergy(final int maxReceive, final boolean simulate) {
        if (!canReceive()) return 0;
        return parent.receiveEnergy(maxReceive, simulate);
    }

    @Override
    public int extractEnergy(final int maxExtract, final boolean simulate) {
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
        } else {
            return parent.getSideConfig().get(side) == IO_SETTING.IN && parent.getSideConfig().hasOutput();
        }
    }
}
