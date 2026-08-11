package com.almostreliable.energymeter.compat.cct;

import com.almostreliable.energymeter.block.entity.MeterBlockEntity;
import com.almostreliable.energymeter.compat.ICapabilityAdapter;
import com.almostreliable.energymeter.core.Registration;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

import dan200.computercraft.api.peripheral.PeripheralCapability;

public final class CCTCompat {

    private CCTCompat() {}

    public static void init(IEventBus modEventBus) {
        modEventBus.addListener(CCTCompat::registerCapabilities);
    }

    private static ICapabilityAdapter<MeterPeripheral> createMeterPeripheral(MeterBlockEntity entity) {
        return new PeripheralCapabilityAdapter(entity);
    }

    private static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(
            PeripheralCapability.get(),
            Registration.METER_BLOCK_ENTITY.get(),
            (entity, direction) -> createMeterPeripheral(entity).getCapability(direction)
        );
    }
}
