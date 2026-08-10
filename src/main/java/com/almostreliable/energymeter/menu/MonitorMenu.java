package com.almostreliable.energymeter.menu;

import com.almostreliable.energymeter.block.entity.MonitorBlockEntity;
import com.almostreliable.energymeter.core.ModRegistration;

import net.minecraft.world.entity.player.Inventory;

public class MonitorMenu extends SynchronizedContainerMenu<MonitorBlockEntity> {

    public MonitorMenu(int wid, Inventory playerInventory, MonitorBlockEntity blockEntity) {
        super(ModRegistration.MONITOR_MENU.get(), wid, playerInventory, blockEntity);
    }

    @Override
    public void setupDataHandlers() {

    }
}
