package com.almostreliable.energymeter.block.entity;

import com.almostreliable.energymeter.core.Constants;
import com.almostreliable.energymeter.core.Registration;
import com.almostreliable.energymeter.menu.MonitorMenu;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import org.jetbrains.annotations.Nullable;

public class MonitorBlockEntity extends BlockEntity implements TickableMenuBlockEntity, MenuProvider {

    private int width;
    private int height;

    public MonitorBlockEntity(BlockPos pos, BlockState blockState) {
        super(Registration.MONITOR_BLOCK_ENTITY.get(), pos, blockState);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (width != 0) tag.putInt(Constants.MULTIBLOCK_WIDTH_ID, width);
        if (height != 0) tag.putInt(Constants.MULTIBLOCK_HEIGHT_ID, height);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains(Constants.MULTIBLOCK_WIDTH_ID)) width = tag.getInt(Constants.MULTIBLOCK_WIDTH_ID);
        if (tag.contains(Constants.MULTIBLOCK_HEIGHT_ID)) height = tag.getInt(Constants.MULTIBLOCK_HEIGHT_ID);
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int wid, Inventory playerInventory, Player player) {
        return new MonitorMenu(wid, playerInventory, this);
    }

    @Override
    public void tick(ServerLevel level) {

    }

    public void setSize(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }
}
