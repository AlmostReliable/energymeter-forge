package testmod.content;

import com.almostreliable.energymeter.menu.SynchronizedContainerMenu;
import com.almostreliable.energymeter.network.menu.handler.IntegerDataHandler;

import net.minecraft.world.entity.player.Inventory;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import testmod.TestRegistration;

public class EnergyBlockMenu extends SynchronizedContainerMenu<EnergyBlockEntity> {

    private int energyToEmitPerTick;
    private int capacity;

    public EnergyBlockMenu(int wid, Inventory playerInventory, EnergyBlockEntity blockEntity) {
        super(TestRegistration.ENERGY_BLOCK_MENU.get(), wid, playerInventory, blockEntity);
    }

    @Override
    public void setupDataHandlers() {
        menuSynchronizer.addDataHandler(new IntegerDataHandler(blockEntity::getEnergyToEmitPerTick, v -> this.energyToEmitPerTick = v));
        menuSynchronizer.addDataHandler(new IntegerDataHandler(blockEntity::getCapacity, v -> this.capacity = v));
    }

    @OnlyIn(Dist.CLIENT)
    public int getEnergyToEmitPerTick() {
        return energyToEmitPerTick;
    }

    @OnlyIn(Dist.CLIENT)
    public int getCapacity() {
        return capacity;
    }
}
