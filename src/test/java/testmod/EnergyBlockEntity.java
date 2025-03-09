package testmod;

import com.almostreliable.energymeter.block.entity.TickableBlockEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.EnergyStorage;
import net.neoforged.neoforge.energy.IEnergyStorage;

import org.jetbrains.annotations.Nullable;

public class EnergyBlockEntity extends BlockEntity implements TickableBlockEntity {

    private final EnergyStorage energyStorage = new EnergyStorage(100_000);
    private int energyToSendPerTick;

    public EnergyBlockEntity(BlockPos pos, BlockState blockState) {
        super(Registration.ENERGY_BLOCK_ENTITY.get(), pos, blockState);
    }

    @Nullable
    public IEnergyStorage getEnergyCapability(@Nullable Direction direction) {
        return energyStorage;
    }

    @Override
    public void tick(ServerLevel level) {
        if (energyToSendPerTick <= 0) {
            return;
        }

        for (Direction direction : Direction.values()) {
            IEnergyStorage targetEnergyStorage = level.getCapability(
                Capabilities.EnergyStorage.BLOCK,
                worldPosition.relative(direction),
                direction.getOpposite()
            );

            if (targetEnergyStorage == null) continue;

            energyStorage.receiveEnergy(energyToSendPerTick, false);
        }
    }

    public void sendEnergyPerTick(int amountPerTick) {
        energyToSendPerTick = amountPerTick;
    }
}
