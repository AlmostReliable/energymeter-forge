package com.almostreliable.energymeter.compat.cct;

import com.almostreliable.energymeter.block.entity.MeterBlockEntity;
import com.almostreliable.energymeter.compat.ICapabilityAdapter;

import dan200.computercraft.api.ComputerCraftAPI;
import dan200.computercraft.api.peripheral.IPeripheral;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.capabilities.BlockCapability;

import javax.annotation.Nullable;

public class PeripheralAdapter implements ICapabilityAdapter<MeterPeripheral> {

    private static final ResourceLocation PERIPHERAL_ID = ResourceLocation.fromNamespaceAndPath(
        ComputerCraftAPI.MOD_ID,
        "peripheral"
    );
    public static final BlockCapability<IPeripheral, Direction> PERIPHERAL_CAPABILITY = BlockCapability.create(
        PERIPHERAL_ID,
        IPeripheral.class,
        Direction.class
    );

    private final MeterPeripheral peripheral;

    public PeripheralAdapter(MeterBlockEntity entity) {
        this.peripheral = new MeterPeripheral(entity);
    }

    @Override
    public MeterPeripheral getCapability(@Nullable Direction direction) {
        return peripheral;
    }
}
