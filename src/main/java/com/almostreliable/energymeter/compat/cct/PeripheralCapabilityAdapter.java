package com.almostreliable.energymeter.compat.cct;

import com.almostreliable.energymeter.block.entity.MeterBlockEntity;
import com.almostreliable.energymeter.compat.ICapabilityAdapter;

import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.capabilities.BlockCapability;

import dan200.computercraft.api.ComputerCraftAPI;
import dan200.computercraft.api.peripheral.IPeripheral;

import org.jspecify.annotations.Nullable;

class PeripheralCapabilityAdapter implements ICapabilityAdapter<MeterPeripheral> {

    private final MeterPeripheral peripheral;

    PeripheralCapabilityAdapter(MeterBlockEntity entity) {
        this.peripheral = new MeterPeripheral(entity);
    }

    @Override
    public MeterPeripheral getCapability(@Nullable Direction direction) {
        return peripheral;
    }
}
