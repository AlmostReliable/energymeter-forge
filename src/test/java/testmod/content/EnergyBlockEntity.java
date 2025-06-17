package testmod.content;

import com.almostreliable.energymeter.block.entity.TickableMenuBlockEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.IEnergyStorage;

import testmod.TestRegistration;

import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;
import java.util.Map;

public class EnergyBlockEntity extends BlockEntity implements TickableMenuBlockEntity {

    private final ModifiableEnergyStorage energyStorage = new ModifiableEnergyStorage(200_000);
    private final Map<Direction, Integer> energyToSendPerTick = new EnumMap<>(Direction.class);

    public EnergyBlockEntity(BlockPos pos, BlockState blockState) {
        super(TestRegistration.ENERGY_BLOCK_ENTITY.get(), pos, blockState);
        for (Direction direction : Direction.values()) {
            energyToSendPerTick.put(direction, 0);
        }
    }

    @Nullable
    public IEnergyStorage getEnergyCapability(@Nullable Direction ignoredDirection) {
        return energyStorage;
    }

    @Override
    public void tick(ServerLevel level) {
        for (var entry : energyToSendPerTick.entrySet()) {
            Direction direction = entry.getKey();
            int energy = entry.getValue();
            if (energy == 0) continue;

            IEnergyStorage targetEnergyStorage = level.getCapability(
                Capabilities.EnergyStorage.BLOCK,
                worldPosition.relative(direction),
                direction.getOpposite()
            );
            if (targetEnergyStorage == null) continue;

            targetEnergyStorage.receiveEnergy(energy, false);
        }
    }

    public void sendEnergyPerTick(Direction direction, int energy) {
        energyToSendPerTick.put(direction, energy);
    }

    public void setEnergyCapacity(int capacity) {
        energyStorage.setCapacity(capacity);
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return null;
    }
}
