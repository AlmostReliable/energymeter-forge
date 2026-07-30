package com.almostreliable.energymeter.compat;

import com.almostreliable.energymeter.block.entity.MeterBlockEntity;

public interface MeterObserver {

    void onChange(MeterBlockEntity entity);

    void onRemove(MeterBlockEntity entity);
}
