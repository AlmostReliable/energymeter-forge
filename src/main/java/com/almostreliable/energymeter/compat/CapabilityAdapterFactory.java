package com.almostreliable.energymeter.compat;

import com.almostreliable.energymeter.EnergyMeter;
import com.almostreliable.energymeter.ModConstants;
import com.almostreliable.energymeter.block.entity.MeterBlockEntity;
import com.almostreliable.energymeter.compat.cct.MeterPeripheral;
import com.almostreliable.energymeter.compat.cct.PeripheralAdapter;
import com.almostreliable.energymeter.core.Registration;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import javax.annotation.Nullable;

@EventBusSubscriber(modid = ModConstants.MOD_ID)
public final class CapabilityAdapterFactory {

    private static final String CCT_ID = "computercraft";

    private CapabilityAdapterFactory() {}

    /**
     * Creates a new peripheral adapter for the given entity.
     * <p>
     * Ensures that CCT is actually loaded to avoid loading its classes and causing a crash.
     *
     * @param entity The entity to create an adapter for.
     * @return The adapter, or null if CCT is not loaded.
     */
    @SuppressWarnings("StaticMethodOnlyUsedInOneClass")
    @Nullable
    public static ICapabilityAdapter<MeterPeripheral> createMeterPeripheral(MeterBlockEntity entity) {
        if (EnergyMeter.isModLoaded(CCT_ID)) {
            return new PeripheralAdapter(entity);
        }

        return null;
    }

    @SubscribeEvent
    private static void registerCapabilities(RegisterCapabilitiesEvent event) {
        if (!EnergyMeter.isModLoaded(CCT_ID)) return;

        event.registerBlockEntity(
            PeripheralAdapter.PERIPHERAL_CAPABILITY,
            Registration.METER_BLOCK_ENTITY.get(),
            (entity, direction) -> {
                var adapter = createMeterPeripheral(entity);
                return adapter == null ? null : adapter.getCapability(direction);
            }
        );
    }

    @SubscribeEvent
    private static void onServerTick(ServerTickEvent.Post event) {
        if (EnergyMeter.isModLoaded(CCT_ID)) {
            MeterPeripheral.tickAttachedPeripherals();
        }
    }
}
