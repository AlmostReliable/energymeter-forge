package testmod.content;

import net.neoforged.neoforge.energy.EnergyStorage;

public class ModifiableEnergyStorage extends EnergyStorage {

    public ModifiableEnergyStorage(int capacity) {
        super(capacity);
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }
}
