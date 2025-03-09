package com.almostreliable.energymeter.menu;

import com.almostreliable.energymeter.block.entity.MeterBlockEntity;
import com.almostreliable.energymeter.core.Registration;
import com.almostreliable.energymeter.network.menu.handler.DoubleDataHandler;

import net.minecraft.world.entity.player.Inventory;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

public class MeterMenu extends SynchronizedContainerMenu<MeterBlockEntity> {

    private double energyRate;

    public MeterMenu(int wid, Inventory playerInventory, MeterBlockEntity blockEntity) {
        super(Registration.METER_MENU.get(), wid, playerInventory, blockEntity);
        menuSynchronizer.addDataHandler(new DoubleDataHandler(blockEntity::getEnergyRate, r -> this.energyRate = r));
    }

    @OnlyIn(Dist.CLIENT)
    public double getEnergyRate() {
        return energyRate;
    }
}
