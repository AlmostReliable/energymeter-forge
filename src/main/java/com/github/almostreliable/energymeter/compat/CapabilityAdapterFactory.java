package com.github.almostreliable.energymeter.compat;

import com.github.almostreliable.energymeter.compat.cct.MeterPeripheral;
import com.github.almostreliable.energymeter.compat.cct.PeripheralAdapter;
import com.github.almostreliable.energymeter.meter.MeterTile;
import net.minecraftforge.fml.ModList;

import javax.annotation.Nullable;

import static com.github.almostreliable.energymeter.core.Constants.CCT_ID;

public enum CapabilityAdapterFactory {
    ;

    @SuppressWarnings("StaticMethodOnlyUsedInOneClass")
    @Nullable
    public static ICapabilityAdapter<MeterPeripheral> createMeterPeripheral(MeterTile tile) {
        if (ModList.get().isLoaded(CCT_ID)) {
            return new PeripheralAdapter(tile);
        }

        return null;
    }
}
