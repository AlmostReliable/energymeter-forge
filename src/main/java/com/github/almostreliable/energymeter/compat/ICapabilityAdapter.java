package com.github.almostreliable.energymeter.compat;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;

public interface ICapabilityAdapter<T> {

    boolean isCapability(Capability<?> cap);

    LazyOptional<T> getLazy();
}
