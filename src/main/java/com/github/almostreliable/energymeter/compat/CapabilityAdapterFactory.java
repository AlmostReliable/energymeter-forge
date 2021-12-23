package com.github.almostreliable.energymeter.compat;

import com.github.almostreliable.energymeter.compat.cct.MeterPeripheral;
import com.github.almostreliable.energymeter.compat.cct.PeripheralAdapter;
import com.github.almostreliable.energymeter.meter.MeterTile;
import net.minecraftforge.fml.ModList;

import javax.annotation.Nullable;

public class CapabilityAdapterFactory {

    @Nullable
    public static ICapabilityAdapter<MeterPeripheral> createMeterPeripheral(MeterTile tile) {
        if (ModList.get().isLoaded("computercraft")) {
            return new PeripheralAdapter(tile);
        }

        return null;
    }
}
