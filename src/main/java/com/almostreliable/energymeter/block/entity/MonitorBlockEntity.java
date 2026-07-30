package com.almostreliable.energymeter.block.entity;

import com.almostreliable.energymeter.core.Constants;
import com.almostreliable.energymeter.menu.MonitorMenu;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import org.jetbrains.annotations.Nullable;

import static com.almostreliable.energymeter.core.Registration.MONITOR_BLOCK_ENTITY;

public class MonitorBlockEntity extends BlockEntity implements TickableMenuBlockEntity {

    private int width;
    private int height;

    public MonitorBlockEntity(BlockPos pos, BlockState blockState) {
        super(MONITOR_BLOCK_ENTITY.get(), pos, blockState);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        if (width != 0) output.putInt(Constants.MULTIBLOCK_WIDTH_ID, width);
        if (height != 0) output.putInt(Constants.MULTIBLOCK_HEIGHT_ID, height);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        width = input.getIntOr(Constants.MULTIBLOCK_WIDTH_ID, 0);
        height = input.getIntOr(Constants.MULTIBLOCK_HEIGHT_ID, 0);
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
