package com.github.almostreliable.energymeter.component;

import com.github.almostreliable.energymeter.util.TypeEnums.MODE;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.energy.IEnergyStorage;

public interface IMeter {

    /**
     * Handles the receive-requests from the {@link IEnergyStorage}.
     * <p>
     * When transfer mode is active, it will try to equally split all incoming energy to
     * adjacent valid outputs.
     * <p>
     * When consumer mode is active, it will void all incoming energy.
     * <p>
     * This method adjusts values which are required for the transfer rate calculation
     * inside the {@link BlockEntity}'s tick method.
     *
     * @param energy   the amount of energy
     * @param simulate whether it's a simulation
     * @return the energy which was accepted
     */
    int receiveEnergy(int energy, boolean simulate);

    /**
     * Returns the {@link SideConfiguration} of the {@link BlockEntity}.
     *
     * @return the {@link SideConfiguration}
     */
    SideConfiguration getSideConfig();

    /**
     * Returns the {@link BlockEntity}'s current mode.
     *
     * @return the current mode
     */
    MODE getMode();
}
