package com.github.almostreliable.energymeter.meter;

import com.github.almostreliable.energymeter.core.Setup.Containers;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.util.IWorldPosCallable;

public class MeterContainer extends Container {

    private final MeterTile tile;

    public MeterContainer(MeterTile tile, int windowID) {
        super(Containers.METER_CONTAINER.get(), windowID);
        this.tile = tile;
    }

    @Override
    public boolean stillValid(PlayerEntity player) {
        return tile.getLevel() != null &&
            Container.stillValid(IWorldPosCallable.create(tile.getLevel(), tile.getBlockPos()),
                player,
                tile.getBlockState().getBlock()
            );
    }

    public MeterTile getTile() {
        return tile;
    }
}
