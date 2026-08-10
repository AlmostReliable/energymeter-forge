package testmod.content;

import net.neoforged.neoforge.transfer.energy.SimpleEnergyHandler;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

public class ModifiableEnergyStorage extends SimpleEnergyHandler {

    private int rootInsertionCalls;

    public ModifiableEnergyStorage(int capacity) {
        super(capacity);
    }

    @Override
    public int insert(int amount, TransactionContext transaction) {
        if (transaction instanceof Transaction concreteTransaction && concreteTransaction.depth() == 0) {
            rootInsertionCalls++;
        }
        return super.insert(amount, transaction);
    }

    public void setMaxEnergyStored(int capacity) {
        this.capacity = capacity;
    }

    public void setMaxReceive(int max) {
        maxInsert = max;
    }

    public int getRootInsertionCalls() {
        return rootInsertionCalls;
    }

    public void resetRootInsertionCalls() {
        rootInsertionCalls = 0;
    }
}
