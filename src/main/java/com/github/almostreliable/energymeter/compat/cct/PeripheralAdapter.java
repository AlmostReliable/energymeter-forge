package com.github.almostreliable.energymeter.compat.cct;

import com.github.almostreliable.energymeter.compat.ICapabilityAdapter;
import com.github.almostreliable.energymeter.meter.MeterTile;
import dan200.computercraft.shared.Capabilities;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;

public class PeripheralAdapter implements ICapabilityAdapter<MeterPeripheral> {

    private final LazyOptional<MeterPeripheral> lazy;

    public PeripheralAdapter(MeterTile tile) {
        lazy = LazyOptional.of(() -> new MeterPeripheral(tile));
    }

    @Override
    public boolean isCapability(Capability<?> cap) {
        return cap == Capabilities.CAPABILITY_PERIPHERAL;
    }

    @Override
    public LazyOptional<MeterPeripheral> getLazy() {
        return lazy;
    }
}
