package com.github.almostreliable.energymeter.meter;

public interface IMeterTileObserver {

    void onMeterTileChanged(MeterTile tile, int flags);

    void onMeterTileRemoved(MeterTile tile);
}
