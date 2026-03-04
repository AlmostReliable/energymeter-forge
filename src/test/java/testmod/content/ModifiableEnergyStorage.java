package testmod.content;

import net.neoforged.neoforge.energy.EnergyStorage;

public class ModifiableEnergyStorage extends EnergyStorage {

    private boolean scheduledInsertionBlock;
    private boolean insertionBlocked;

    public ModifiableEnergyStorage(int capacity) {
        super(capacity);
    }

    @Override
    public int receiveEnergy(int toReceive, boolean simulate) {
        if (insertionBlocked) return 0;

        if (!simulate && scheduledInsertionBlock) {
            // block insertion for next operation
            insertionBlocked = true;
        }

        return super.receiveEnergy(toReceive, simulate);
    }

    public void setMaxEnergyStored(int capacity) {
        this.capacity = capacity;
    }

    public void setMaxReceive(int max) {
        maxReceive = max;
    }

    public void scheduleInsertionBlock() {
        scheduledInsertionBlock = true;
    }
}
