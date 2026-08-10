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

    private static final Identifier PERIPHERAL_ID = Identifier.fromNamespaceAndPath(
        ComputerCraftAPI.MOD_ID,
        "peripheral"
    );
    static final BlockCapability<IPeripheral, Direction> PERIPHERAL_CAPABILITY = BlockCapability.create(
        PERIPHERAL_ID,
        IPeripheral.class,
        Direction.class
    );

    private final MeterPeripheral peripheral;

    PeripheralCapabilityAdapter(MeterBlockEntity entity) {
        this.peripheral = new MeterPeripheral(entity);
    }

    @Override
    public MeterPeripheral getCapability(@Nullable Direction direction) {
        return peripheral;
    }
}
