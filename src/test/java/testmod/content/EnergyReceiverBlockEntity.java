package testmod.content;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;

import testmod.TestRegistration;

import org.jspecify.annotations.Nullable;

public class EnergyReceiverBlockEntity extends BlockEntity {

    private final ModifiableEnergyStorage energyStorage = new ModifiableEnergyStorage(200_000);

    public EnergyReceiverBlockEntity(BlockPos pos, BlockState blockState) {
        super(TestRegistration.ENERGY_RECEIVER_BLOCK_ENTITY.get(), pos, blockState);
    }

    @Nullable
    public EnergyHandler getEnergyCapability(@Nullable Direction ignoredDirection) {
        return energyStorage;
    }

    public void setCapacity(int capacity) {
        energyStorage.setMaxEnergyStored(capacity);
    }

    public void setMaxReceive(int max) {
        energyStorage.setMaxReceive(max);
    }

    public void scheduleInsertionBlock() {
        energyStorage.scheduleInsertionBlock();
    }
}
