package com.github.almostreliable.energymeter.compat;

import com.github.almostreliable.energymeter.meter.MeterTile;

public interface IMeterTileObserver {

    /**
     * Called whenever data on the Energy Meter changes in order to invoke a CCT event.
     *
     * @param tile  the Energy Meter tile
     * @param flags the data flags to identify the data that changed
     */
    void onMeterTileChanged(MeterTile tile, int flags);

    /**
     * Called when the Energy Meter was removed in order to invoke a CCT event.
     *
     * @param tile the Energy Meter tile
     */
    void onMeterTileRemoved(MeterTile tile);
}
