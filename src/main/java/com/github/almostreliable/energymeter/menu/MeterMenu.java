package com.github.almostreliable.energymeter.menu;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.ItemStack;

import com.github.almostreliable.energymeter.block.entity.MeterBlockEntity;
import com.github.almostreliable.energymeter.core.Registration;

import java.util.Objects;

public class MeterMenu extends AbstractContainerMenu {

    private final MeterBlockEntity entity;

    public MeterMenu(MeterBlockEntity entity, int wid) {
        super(Registration.METER_MENU.get(), wid);
        this.entity = entity;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int slot) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(
            ContainerLevelAccess.create(Objects.requireNonNull(entity.getLevel()), entity.getBlockPos()),
            player,
            entity.getBlockState().getBlock()
        );
    }

    public MeterBlockEntity getEntity() {
        return entity;
    }
}
