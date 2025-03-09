package com.github.almostreliable.energymeter.block.component;

import com.github.almostreliable.energymeter.util.TypeEnums;
import com.google.common.primitives.Ints;
import net.neoforged.neoforge.energy.IEnergyStorage;

import java.util.function.Function;
import java.util.function.Supplier;

public final class ForwardingEnergyStorage implements IEnergyStorage {

    private final EnergyHandler energyHandler;
    private final Supplier<TypeEnums.IoSetting> settingSupplier;

    public ForwardingEnergyStorage(EnergyHandler energyHandler, Supplier<TypeEnums.IoSetting> settingSupplier) {
        this.energyHandler = energyHandler;
        this.settingSupplier = settingSupplier;
    }

    @Override
    public int receiveEnergy(int amount, boolean simulate) {
        if (!canReceive()) return 0;
        return energyHandler.forwardEnergy(amount, simulate);
    }

    @Override
    public int extractEnergy(int amount, boolean simulate) {
        return 0;
    }

    @Override
    public int getEnergyStored() {
        if (!canReceive()) return 0;
        return forwardToOutputs(IEnergyStorage::getEnergyStored);
    }

    @Override
    public int getMaxEnergyStored() {
        if (!canReceive()) return 0;
        return forwardToOutputs(IEnergyStorage::getMaxEnergyStored);
    }

    @Override
    public boolean canReceive() {
        return settingSupplier.get() == TypeEnums.IoSetting.IN;
    }

    @Override
    public boolean canExtract() {
        return false;
    }

    private int forwardToOutputs(Function<IEnergyStorage, Integer> energyGetter) {
        long result = 0;

        for (IEnergyStorage neighborEnergyStorage : energyHandler.getValidOutputEnergyStorages()) {
            result += energyGetter.apply(neighborEnergyStorage);
        }

        return Ints.saturatedCast(result);
    }
}
