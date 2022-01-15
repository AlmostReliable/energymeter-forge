package com.github.almostreliable.energymeter.compat;

import com.github.almostreliable.energymeter.compat.cct.MeterPeripheral;
import com.github.almostreliable.energymeter.compat.cct.PeripheralAdapter;
import com.github.almostreliable.energymeter.meter.MeterEntity;
import net.minecraftforge.fml.ModList;

import javax.annotation.Nullable;

import static com.github.almostreliable.energymeter.core.Constants.CCT_ID;

public final class CapabilityAdapterFactory {

    private CapabilityAdapterFactory() {}

    @SuppressWarnings("StaticMethodOnlyUsedInOneClass")
    @Nullable
    public static ICapabilityAdapter<MeterPeripheral> createMeterPeripheral(MeterEntity entity) {
        if (ModList.get().isLoaded(CCT_ID)) {
            return new PeripheralAdapter(entity);
        }

        return null;
    }
}
