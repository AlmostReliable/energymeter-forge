package com.github.almostreliable.energymeter.component;

import com.github.almostreliable.energymeter.util.TypeEnums;
import com.github.almostreliable.energymeter.util.TypeEnums.MODE;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.energy.IEnergyStorage;

public interface IMeter {

    /**
     * Handles the receive-requests from the {@link IEnergyStorage}.
     * When transfer mode is active, it will try to equally split all incoming energy to
     * adjacent valid outputs.
     * When consumer mode is active, it will void all incoming energy.
     * This method adjusts values which are required for the flow rate calculation
     * inside the {@link TileEntity}'s tick method.
     *
     * @param energy   the amount of energy
     * @param simulate whether it's a simulation
     * @return the energy which was accepted
     */
    int receiveEnergy(int energy, boolean simulate);

    /**
     * Returns the {@link SideConfiguration} of the {@link TileEntity}.
     *
     * @return the side configuration
     */
    SideConfiguration getSideConfig();

    /**
     * Returns whether the {@link TileEntity} is currently set as consumer.
     * This means it voids all incoming energy.
     *
     * @return true if consumer, false otherwise
     */
    MODE getMode();
}
