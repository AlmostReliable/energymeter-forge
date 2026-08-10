package testmod.content;

import com.almostreliable.energymeter.block.entity.TickableMenuBlockEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
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

public class EnergyEmitterBlockEntity extends BlockEntity implements TickableMenuBlockEntity {

    private static final Direction[] DIRECTIONS = Direction.values();
    private static final String ENERGY_TO_EMIT_PER_TICK_ID = "energy_to_emit_per_tick";

    private int energyToEmitPerTick;

    public EnergyEmitterBlockEntity(BlockPos pos, BlockState blockState) {
        super(TestRegistration.ENERGY_EMITTER_BLOCK_ENTITY.get(), pos, blockState);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt(ENERGY_TO_EMIT_PER_TICK_ID, energyToEmitPerTick);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains(ENERGY_TO_EMIT_PER_TICK_ID)) energyToEmitPerTick = tag.getInt(ENERGY_TO_EMIT_PER_TICK_ID);
    }

    @Override
    public void tick(ServerLevel level) {
        if (energyToEmitPerTick == 0) return;

        for (var direction : DIRECTIONS) {
            IEnergyStorage targetEnergyStorage = level.getCapability(
                Capabilities.EnergyStorage.BLOCK,
                worldPosition.relative(direction),
                direction.getOpposite()
            );
            if (targetEnergyStorage == null) continue;

            targetEnergyStorage.receiveEnergy(energyToEmitPerTick, false);
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
