package com.github.almostreliable.energymeter.menu;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.ItemStack;

import com.github.almostreliable.energymeter.core.Registration;

public class ScreenMenu extends AbstractContainerMenu {

    private final ContainerLevelAccess access;

    public ScreenMenu(int wid, Inventory ignoredPlayerInventory, ContainerLevelAccess access) {
        super(Registration.SCREEN_MENU.get(), wid);
        this.access = access;
    }

    public ScreenMenu(int wid, Inventory playerInventory) {
        this(wid, playerInventory, ContainerLevelAccess.NULL);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(access, player, Registration.SCREEN_BLOCK.get());
    }
}
