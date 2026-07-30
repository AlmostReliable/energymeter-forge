package testmod.content;

import net.neoforged.neoforge.transfer.energy.SimpleEnergyHandler;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

public class ModifiableEnergyStorage extends SimpleEnergyHandler {

    private boolean scheduledInsertionBlock;
    private boolean insertionBlocked;

    public ModifiableEnergyStorage(int capacity) {
        super(capacity);
    }

    @Override
    public int insert(int amount, TransactionContext transaction) {
        if (insertionBlocked) return 0;
        return super.insert(amount, transaction);
    }

    @Override
    protected void onEnergyChanged(int previousAmount) {
        if (scheduledInsertionBlock) insertionBlocked = true;
    }

    public void setMaxEnergyStored(int capacity) {
        this.capacity = capacity;
    }

    public void setMaxReceive(int max) {
        maxInsert = max;
    }

    public void scheduleInsertionBlock() {
        scheduledInsertionBlock = true;
    }
}
