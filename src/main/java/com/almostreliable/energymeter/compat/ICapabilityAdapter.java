package com.almostreliable.energymeter.compat;

import net.minecraft.core.Direction;

import org.jetbrains.annotations.Nullable;

@FunctionalInterface
public interface ICapabilityAdapter<T> {

    T getCapability(@Nullable Direction direction);
}
