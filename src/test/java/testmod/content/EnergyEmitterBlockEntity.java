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
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.transaction.Transaction;

import testmod.TestRegistration;

import org.jspecify.annotations.Nullable;

public class EnergyEmitterBlockEntity extends BlockEntity implements TickableMenuBlockEntity {

    private static final Direction[] DIRECTIONS = Direction.values();
    private static final String ENERGY_TO_EMIT_PER_TICK_ID = "energy_to_emit_per_tick";

    private int energyToEmitPerTick;

    public EnergyEmitterBlockEntity(BlockPos pos, BlockState blockState) {
        super(TestRegistration.ENERGY_RECEIVER_BLOCK_ENTITY.get(), pos, blockState);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putInt(ENERGY_TO_EMIT_PER_TICK_ID, energyToEmitPerTick);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        energyToEmitPerTick = input.getIntOr(ENERGY_TO_EMIT_PER_TICK_ID, energyToEmitPerTick);
    }

    @Override
    public void tick(ServerLevel level) {
        if (energyToEmitPerTick == 0) return;

        for (var direction : DIRECTIONS) {
            EnergyHandler targetEnergyStorage = level.getCapability(
                Capabilities.Energy.BLOCK,
                worldPosition.relative(direction),
                direction.getOpposite()
            );
            if (targetEnergyStorage == null) continue;

            try (Transaction transaction = Transaction.openRoot()) {
                targetEnergyStorage.insert(energyToEmitPerTick, transaction);
                transaction.commit();
            }
        }
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new EnergyEmitterBlockMenu(containerId, playerInventory, this);
    }

    public void setEnergyToEmitPerTick(int energyToEmitPerTick) {
        this.energyToEmitPerTick = energyToEmitPerTick;
    }

    public int getEnergyToEmitPerTick() {
        return energyToEmitPerTick;
    }
}
