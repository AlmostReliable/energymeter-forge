package com.github.almostreliable.energymeter.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import com.github.almostreliable.energymeter.block.multiblock.MultiblockData;
import com.github.almostreliable.energymeter.core.Constants;
import com.github.almostreliable.energymeter.core.Registration;
import com.github.almostreliable.energymeter.menu.MonitorMenu;
import com.github.almostreliable.energymeter.util.TextUtils;
import com.github.almostreliable.energymeter.util.TypeEnums;

import org.jetbrains.annotations.Nullable;

public class MonitorBlockEntity extends BlockEntity implements TickableMenuProvider {

    @Nullable
    private MultiblockData data;

    public MonitorBlockEntity(BlockPos pos, BlockState blockState) {
        super(Registration.MONITOR_BLOCK_ENTITY.get(), pos, blockState);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (data != null) tag.put(Constants.MULTIBLOCK_DATA_ID, data.serialize());
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains(Constants.MULTIBLOCK_DATA_ID)) data = MultiblockData.deserialize(tag.getCompound(Constants.MULTIBLOCK_DATA_ID));
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int wid, Inventory playerInventory, Player player) {
        if (level == null) return null;
        return new MonitorMenu(wid, playerInventory, ContainerLevelAccess.create(level, worldPosition));
    }

    @Override
    public Component getDisplayName() {
        return TextUtils.translate(TypeEnums.TRANSLATE_TYPE.CONTAINER, Constants.MONITOR_ID);
    }

    @Override
    public void tick(ServerLevel level) {

    }

    public void setMultiblockData(MultiblockData data) {
        this.data = data;
    }

    @Nullable
    public MultiblockData getMultiblockData() {
        return data;
    }
}
