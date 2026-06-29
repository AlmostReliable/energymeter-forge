package com.almostreliable.energymeter.compat;

import net.minecraft.core.Direction;

import javax.annotation.Nullable;

public interface ICapabilityAdapter<T> {

    /**
     * Gets the adapted capability instance for the given side.
     *
     * @param direction The side the capability is queried from.
     * @return The capability instance.
     */
    T getCapability(@Nullable Direction direction);
}
