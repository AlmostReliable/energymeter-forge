package com.github.almostreliable.energymeter.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import com.github.almostreliable.energymeter.core.Constants;
import com.github.almostreliable.energymeter.core.Registration;
import com.github.almostreliable.energymeter.menu.ScreenMenu;
import com.github.almostreliable.energymeter.util.TextUtils;
import com.github.almostreliable.energymeter.util.TypeEnums;

import org.jetbrains.annotations.Nullable;

public class ScreenBlockEntity extends BlockEntity implements MenuProvider {

    public ScreenBlockEntity(BlockPos pos, BlockState blockState) {
        super(Registration.SCREEN_BLOCK_ENTITY.get(), pos, blockState);
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int wid, Inventory playerInventory, Player player) {
        if (level == null) return null;
        return new ScreenMenu(wid, playerInventory, ContainerLevelAccess.create(level, worldPosition));
    }

    @Override
    public Component getDisplayName() {
        return TextUtils.translate(TypeEnums.TRANSLATE_TYPE.CONTAINER, Constants.SCREEN_ID);
    }
}
