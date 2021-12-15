package com.github.almostreliable.energymeter.meter;

import com.github.almostreliable.energymeter.core.Setup.Containers;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.Objects;

public class MeterContainer extends AbstractContainerMenu {

    private final MeterEntity entity;

    public MeterContainer(MeterEntity entity, int windowID) {
        super(Containers.METER.get(), windowID);
        this.entity = entity;
    }

    /**
     * Gets the parent {@link BlockEntity} of the {@link Container}.
     *
     * @return the parent
     */
    public MeterEntity getEntity() {
        return entity;
    }

    @Override
    public boolean stillValid(Player player) {
        return AbstractContainerMenu.stillValid(
            ContainerLevelAccess.create(Objects.requireNonNull(entity.getLevel()), entity.getBlockPos()),
            player,
            entity.getBlockState().getBlock()
        );
    }
}
