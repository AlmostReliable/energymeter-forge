package com.almostreliable.energymeter.compat;

import com.almostreliable.energymeter.block.entity.MeterBlockEntity;
import com.almostreliable.energymeter.compat.cct.MeterPeripheral;
import com.almostreliable.energymeter.compat.cct.PeripheralAdapter;

import net.neoforged.fml.ModList;

import javax.annotation.Nullable;

import static com.almostreliable.energymeter.core.Constants.CCT_ID;

public final class CapabilityAdapterFactory {

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
        if (ModList.get().isLoaded(CCT_ID)) {
            return new PeripheralAdapter(entity);
        }

        return null;
    }
}
