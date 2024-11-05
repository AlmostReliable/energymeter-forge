package com.github.almostreliable.energymeter.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public interface TickableMenuProvider extends MenuProvider {

    @SuppressWarnings("StaticMethodOnlyUsedInOneClass")
    static <E extends BlockEntity> void tick(Level level, BlockPos ignoredPos, BlockState ignoredState, E blockEntity) {
        if (level instanceof ServerLevel serverLevel && blockEntity instanceof TickableMenuProvider tickableMenuProvider) {
            tickableMenuProvider.tick(serverLevel);
        }
    }

    void tick(ServerLevel level);
}
