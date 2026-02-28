package testmod.content;

import net.neoforged.neoforge.energy.EnergyStorage;

public class ModifiableEnergyStorage extends EnergyStorage {

    public ModifiableEnergyStorage(int capacity) {
        super(capacity);
    }

    public void setMaxEnergyStored(int capacity) {
        this.capacity = capacity;
    }
}
