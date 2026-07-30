package com.almostreliable.energymeter.compat;

import net.minecraft.core.Direction;

import javax.annotation.Nullable;

@FunctionalInterface
public interface ICapabilityAdapter<T> {

    T getCapability(@Nullable Direction direction);
}
