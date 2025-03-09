package com.github.almostreliable.energymeter.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import com.github.almostreliable.energymeter.core.Constants;
import com.github.almostreliable.energymeter.core.Registration;
import com.github.almostreliable.energymeter.menu.MonitorMenu;
import com.github.almostreliable.energymeter.util.TextUtils;
import com.github.almostreliable.energymeter.util.TypeEnums;

import org.jetbrains.annotations.Nullable;

public class MonitorBlockEntity extends BlockEntity implements TickableBlockEntity, MenuProvider {

    private int width;
    private int height;

    public MonitorBlockEntity(BlockPos pos, BlockState blockState) {
        super(Registration.MONITOR_BLOCK_ENTITY.get(), pos, blockState);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (width != 0) tag.putInt(Constants.WIDTH_ID, width);
        if (height != 0) tag.putInt(Constants.HEIGHT_ID, height);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains(Constants.WIDTH_ID)) width = tag.getInt(Constants.WIDTH_ID);
        if (tag.contains(Constants.HEIGHT_ID)) height = tag.getInt(Constants.HEIGHT_ID);
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int wid, Inventory playerInventory, Player player) {
        if (level == null) return null;
        return new MonitorMenu(wid, playerInventory, ContainerLevelAccess.create(level, worldPosition));
    }

    @Override
    public Component getDisplayName() {
        // TODO: replace with datagen or leave empty
        return TextUtils.translate(TypeEnums.TranslateType.CONTAINER, Constants.MONITOR_ID);
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
