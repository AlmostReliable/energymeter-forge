package com.almostreliable.energymeter.compat.cct;

import com.almostreliable.energymeter.block.entity.MeterBlockEntity;
import com.almostreliable.energymeter.compat.ICapabilityAdapter;
import com.almostreliable.energymeter.core.Registration;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

public final class CCTCompat {

    private CCTCompat() {}

    public static void init(IEventBus modEventBus) {
        modEventBus.addListener(CCTCompat::registerCapabilities);
        NeoForge.EVENT_BUS.addListener(CCTCompat::onServerTick);
    }

    private static ICapabilityAdapter<MeterPeripheral> createMeterPeripheral(MeterBlockEntity entity) {
        return new PeripheralAdapter(entity);
    }

    private static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(
            PeripheralAdapter.PERIPHERAL_CAPABILITY,
            Registration.METER_BLOCK_ENTITY.get(),
            (entity, direction) -> createMeterPeripheral(entity).getCapability(direction)
        );
    }

    private static void onServerTick(ServerTickEvent.Post event) {
        MeterPeripheral.tickAttachedPeripherals();
    }
}
