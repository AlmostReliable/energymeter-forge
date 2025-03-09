package com.almostreliable.energymeter.compat;

import com.almostreliable.energymeter.block.entity.MeterBlockEntity;

public interface IMeterEntityObserver {

    /**
     * Called whenever data on the Energy Meter changes in order to invoke a CCT event.
     *
     * @param entity the Energy Meter tile
     * @param flags  the data flags to identify the data that changed
     */
    void onMeterTileChanged(MeterBlockEntity entity, int flags);

    /**
     * Called when the Energy Meter was removed in order to invoke a CCT event.
     *
     * @param entity the Energy Meter tile
     */
    void onMeterTileRemoved(MeterBlockEntity entity);
}
