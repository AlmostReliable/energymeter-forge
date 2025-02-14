package com.github.almostreliable.energymeter.menu;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.ItemStack;

import com.github.almostreliable.energymeter.core.Registration;

public class MeterMenu extends AbstractContainerMenu {

    private final ContainerLevelAccess access;

    public MeterMenu(int wid, Inventory ignoredPlayerInventory, ContainerLevelAccess access) {
        super(Registration.METER_MENU.get(), wid);
        this.access = access;
    }

    public MeterMenu(int wid, Inventory playerInventory) {
        this(wid, playerInventory, ContainerLevelAccess.NULL);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int slot) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(access, player, Registration.METER_BLOCK.get());
    }
}
