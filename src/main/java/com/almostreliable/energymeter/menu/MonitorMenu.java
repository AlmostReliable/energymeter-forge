package com.almostreliable.energymeter.menu;

import com.almostreliable.energymeter.block.entity.MonitorBlockEntity;
import com.almostreliable.energymeter.core.Registration;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;

public class MonitorMenu extends SynchronizedContainerMenu<MonitorBlockEntity> {

    public MonitorMenu(int wid, Inventory playerInventory, MonitorBlockEntity blockEntity) {
        super(Registration.MONITOR_MENU.get(), wid, playerInventory, blockEntity);
    }

    @Override
    public void setupDataHandlers() {

    }

    @Override
    public void receiveClientData(ServerPlayer player, CompoundTag data) {

    }
}
