package com.almostreliable.energymeter.block.component;

import com.almostreliable.energymeter.util.TypeEnums;

import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.neoforge.capabilities.BlockCapabilityCache;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.IEnergyStorage;

import com.google.common.primitives.Ints;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EnergyHandler {

    private final EnergyHandlerHost host;
    private final Map<Direction, ForwardingEnergyStorage> energyStorage = new EnumMap<>(Direction.class);
    private final List<Double> energyPerIntervalHistory = Collections.synchronizedList(new ArrayList<>());
    private final Map<Direction, BlockCapabilityCache<IEnergyStorage, Direction>> outputCache = new EnumMap<>(Direction.class);

    private double energyPerInterval;

    public EnergyHandler(EnergyHandlerHost host, Direction facing) {
        this.host = host;

        for (Direction direction : Direction.values()) {
            if (direction == facing) continue;
            energyStorage.put(direction, new ForwardingEnergyStorage(this, () -> host.getIoConfig().getSetting(direction)));
        }
    }

    public IEnergyStorage getEnergyStorage(Direction direction) {
        return energyStorage.get(direction);
    }

    public boolean hasHistory() {
        return !energyPerIntervalHistory.isEmpty();
    }

    private boolean hasValidOutput() {
        for (var cache : outputCache.values()) {
            if (cache != null) return true;
        }
        return false;
    }

    public void resetHistory() {
        energyPerIntervalHistory.clear();
    }

    public void resetHistory(double initialValue) {
        resetHistory();
        energyPerIntervalHistory.add(initialValue);
    }

    public double getAverage() {
        double sum = 0;
        for (double energy : energyPerIntervalHistory) {
            sum += energy;
        }
        return sum / energyPerIntervalHistory.size();
    }

    public void intervalReached() {
        energyPerIntervalHistory.add(energyPerInterval);
        energyPerInterval = 0;
    }

    public int forwardEnergy(int amount, boolean simulate) {
        if (host.getTransferMode() == TypeEnums.TransferMode.CONSUME) {
            if (!simulate) energyPerInterval += amount;
            return amount;
        }

        MaxEnergyPerOutputResult maxEnergyPerOutputResult = calculateMaxEnergyPerOutput(amount);
        var maxEnergyPerOutput = maxEnergyPerOutputResult.maxEnergyPerOutput;
        int maxEnergyPerOutputSum = maxEnergyPerOutputResult.maxEnergyPerOutputSum;

        if (maxEnergyPerOutputSum <= 0) return 0;
        if (simulate) return Math.min(maxEnergyPerOutputSum, amount);

        if (maxEnergyPerOutputSum <= amount) {
            fillOutputsWithMaxEnergy(maxEnergyPerOutput);
            energyPerInterval += maxEnergyPerOutputSum;
            return maxEnergyPerOutputSum;
        }

        int energyForwarded = splitEnergyBetweenOutputs(maxEnergyPerOutput, amount);
        energyPerInterval += energyForwarded;
        return energyForwarded;
    }

    private MaxEnergyPerOutputResult calculateMaxEnergyPerOutput(int maxEnergyToForward) {
        Map<IEnergyStorage, Integer> energyPerOutput = new HashMap<>();
        long maxEnergyPerOutputSum = 0;

        for (IEnergyStorage neighborEnergyStorage : getValidOutputEnergyStorages()) {
            int maxEnergyPerOutput = neighborEnergyStorage.receiveEnergy(maxEnergyToForward, true);
            if (maxEnergyPerOutput > 0) {
                energyPerOutput.put(neighborEnergyStorage, maxEnergyPerOutput);
                maxEnergyPerOutputSum += maxEnergyPerOutput;
            }
        }

        return new MaxEnergyPerOutputResult(energyPerOutput, Ints.saturatedCast(maxEnergyPerOutputSum));
    }

    public Iterable<IEnergyStorage> getValidOutputEnergyStorages() {
        List<IEnergyStorage> result = new ArrayList<>();

        for (Direction direction : Direction.values()) {
            var capabilityCache = getOrSetupCache(direction);
            if (capabilityCache == null) continue;
            IEnergyStorage neighborEnergyStorage = capabilityCache.getCapability();
            if (neighborEnergyStorage == null) continue;

            result.add(neighborEnergyStorage);
        }

        return result;
    }

    private void fillOutputsWithMaxEnergy(Map<IEnergyStorage, Integer> maxEnergyPerOutput) {
        for (var outputEntry : maxEnergyPerOutput.entrySet()) {
            IEnergyStorage neighborEnergyStorage = outputEntry.getKey();
            int energyToReceiveMax = outputEntry.getValue();
            neighborEnergyStorage.receiveEnergy(energyToReceiveMax, false);
        }
    }

    private int splitEnergyBetweenOutputs(Map<IEnergyStorage, Integer> energyPerOutput, int maxEnergyToForward) {
        var energyToForward = maxEnergyToForward;
        var energyForwarded = 0;

        while (!energyPerOutput.isEmpty() && energyToForward >= energyPerOutput.size()) {
            var energyToForwardPerOutput = energyToForward / energyPerOutput.size();
            var fullOutputs = new ArrayList<IEnergyStorage>();

            for (var output : energyPerOutput.entrySet()) {
                IEnergyStorage neighborEnergyStorage = output.getKey();
                int maxEnergyForOutput = output.getValue();
                var energyToForwardForOutput = energyToForwardPerOutput;

                if (maxEnergyForOutput < energyToForwardForOutput) {
                    energyToForwardForOutput = maxEnergyForOutput;
                    fullOutputs.add(neighborEnergyStorage);
                }

                neighborEnergyStorage.receiveEnergy(energyToForwardForOutput, false);
                energyToForward -= energyToForwardForOutput;
                energyForwarded += energyToForwardForOutput;
            }

            fullOutputs.forEach(energyPerOutput::remove);
        }

        return energyForwarded;
    }

    // private BlockCapabilityCache<IEnergyStorage, Direction> getOrSetupCache() {
    //     if (cache != null) {
    //         return cache;
    //     }
    //
    //     return BlockCapabilityCache.create(
    //         Capabilities.EnergyStorage.BLOCK,
    //         (ServerLevel) host.getLevel(),
    //         host.getBlockPos().relative(direction),
    //         direction.getOpposite(),
    //         () -> !host.isRemoved(),
    //         () -> cache = null
    //     );
    // }

    private record MaxEnergyPerOutputResult(Map<IEnergyStorage, Integer> maxEnergyPerOutput, int maxEnergyPerOutputSum) {}
}
