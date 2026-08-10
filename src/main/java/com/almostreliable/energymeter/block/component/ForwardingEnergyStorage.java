package com.almostreliable.energymeter.block.component;

import com.almostreliable.energymeter.block.component.IoConfig.IoSettingWithPriority;

import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

import java.util.function.Supplier;
import java.util.function.ToLongFunction;

public final class ForwardingEnergyStorage implements EnergyHandler {

    private final MeterEnergyHandler energyHandler;
    private final Supplier<IoSettingWithPriority> settingSupplier;

    public ForwardingEnergyStorage(MeterEnergyHandler energyHandler, Supplier<IoSettingWithPriority> settingSupplier) {
        this.energyHandler = energyHandler;
        this.settingSupplier = settingSupplier;
    }

    @Override
    public int insert(int amount, TransactionContext transaction) {
        if (!settingSupplier.get().isInput()) return 0;
        return energyHandler.forwardEnergy(amount, transaction);
    }

    @Override
    public int extract(int amount, TransactionContext transaction) {
        return 0;
    }

    @Override
    public long getAmountAsLong() {
        if (!settingSupplier.get().isInput()) return 0;
        return sumOutputs(EnergyHandler::getAmountAsLong);
    }

    @Override
    public long getCapacityAsLong() {
        if (!settingSupplier.get().isInput()) return 0;
        return sumOutputs(EnergyHandler::getCapacityAsLong);
    }

    private long sumOutputs(ToLongFunction<EnergyHandler> energyGetter) {
        return sumOutputs(energyHandler.getValidOutputEnergyStorages(), energyGetter);
    }

    private static long sumOutputs(Iterable<EnergyHandler> outputs, ToLongFunction<EnergyHandler> energyGetter) {
        long result = 0;

        for (EnergyHandler output : outputs) {
            long value = energyGetter.applyAsLong(output);
            if (value <= 0) continue;
            if (value > Long.MAX_VALUE - result) return Long.MAX_VALUE;
            result += value;
        }

        return result;
    }
}
