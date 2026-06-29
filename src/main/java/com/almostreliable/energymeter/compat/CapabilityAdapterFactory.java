package com.almostreliable.energymeter.compat;

import com.almostreliable.energymeter.EnergyMeter;
import com.almostreliable.energymeter.compat.cct.CCTCompat;

import net.neoforged.bus.api.IEventBus;

public final class CapabilityAdapterFactory {

    private static final String CCT_ID = "computercraft";

    private CapabilityAdapterFactory() {}

    public static void init(IEventBus modEventBus) {
        if (EnergyMeter.isModLoaded(CCT_ID)) {
            CCTCompat.init(modEventBus);
        }
    }
}
