package testmod.content;

import com.almostreliable.energymeter.menu.SynchronizedContainerMenu;
import com.almostreliable.energymeter.network.menu.handler.IntegerDataHandler;

import net.minecraft.world.entity.player.Inventory;

import testmod.TestRegistration;

public class EnergyEmitterBlockMenu extends SynchronizedContainerMenu<EnergyEmitterBlockEntity> {

    private int energyToEmitPerTick;

    public EnergyEmitterBlockMenu(int wid, Inventory playerInventory, EnergyEmitterBlockEntity blockEntity) {
        super(TestRegistration.ENERGY_EMITTER_BLOCK_MENU.get(), wid, playerInventory, blockEntity);
    }

    @Override
    public void setupDataHandlers() {
        menuSynchronizer.addDataHandler(new IntegerDataHandler(blockEntity::getEnergyToEmitPerTick, v -> this.energyToEmitPerTick = v));
    }

    public int getEnergyToEmitPerTick() {
        return energyToEmitPerTick;
    }
}
