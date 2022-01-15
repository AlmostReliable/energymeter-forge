package com.github.almostreliable.energymeter.compat;

import com.github.almostreliable.energymeter.meter.MeterEntity;

public interface IMeterTileObserver {

    void onMeterTileChanged(MeterEntity entity, int flags);

    void onMeterTileRemoved(MeterEntity entity);
}
