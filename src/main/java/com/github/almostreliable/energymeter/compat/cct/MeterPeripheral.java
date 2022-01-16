package com.github.almostreliable.energymeter.compat.cct;

import com.github.almostreliable.energymeter.compat.IMeterEntityObserver;
import com.github.almostreliable.energymeter.meter.MeterEntity;
import dan200.computercraft.api.lua.LuaFunction;
import dan200.computercraft.api.lua.MethodResult;
import dan200.computercraft.api.peripheral.IComputerAccess;
import dan200.computercraft.api.peripheral.IPeripheral;
import net.minecraft.core.Direction;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

import static com.github.almostreliable.energymeter.core.Constants.*;

@SuppressWarnings({"unused", "FinalMethod"})
public class MeterPeripheral implements IPeripheral, IMeterEntityObserver {

    private final MeterEntity entity;
    private IComputerAccess computer;

    MeterPeripheral(MeterEntity entity) {
        this.entity = entity;
    }

    @LuaFunction(mainThread = true)
    public final MethodResult getSideConfig(Direction direction) {
        return MethodResult.of(entity.getSideConfig().get(direction).name());
    }

    @LuaFunction(mainThread = true)
    public final MethodResult hasInput() {
        return MethodResult.of(entity.getSideConfig().hasInput());
    }

    @LuaFunction(mainThread = true)
    public final MethodResult hasOutput() {
        return MethodResult.of(entity.getSideConfig().hasOutput());
    }

    @LuaFunction(mainThread = true)
    public final MethodResult hasMaxOutputs() {
        return MethodResult.of(entity.getSideConfig().hasMaxOutputs());
    }

    @Override
    public void onMeterTileChanged(MeterEntity entity, int flags) {
        if (!this.entity.equals(entity)) return;

        Map<String, Object> data = new HashMap<>();
        if ((flags & SYNC_FLAGS.SIDE_CONFIG) != 0) data.put(SIDE_CONFIG_ID, entity.getSideConfig().asStringMap());
        if ((flags & SYNC_FLAGS.TRANSFER_RATE) != 0) data.put(TRANSFER_RATE_ID, entity.getTransferRate());
        if ((flags & SYNC_FLAGS.NUMBER_MODE) != 0) data.put(NUMBER_MODE_ID, entity.getNumberMode().name());
        if ((flags & SYNC_FLAGS.STATUS) != 0) data.put(STATUS_ID, entity.getStatus().name());
        if ((flags & SYNC_FLAGS.MODE) != 0) data.put(MODE_ID, entity.getMode().name());
        if ((flags & SYNC_FLAGS.ACCURACY) != 0) data.put(ACCURACY_ID, entity.getAccuracy().name());
        if ((flags & SYNC_FLAGS.INTERVAL) != 0) data.put(INTERVAL_ID, entity.getInterval());
        if ((flags & SYNC_FLAGS.THRESHOLD) != 0) data.put(THRESHOLD_ID, entity.getThreshold());
        computer.queueEvent("em_data_changed", data);
    }

    @Override
    public void onMeterTileRemoved(MeterEntity entity) {
        computer.queueEvent("em_removed");
    }

    @Nonnull
    @Override
    public String getType() {
        return MOD_ID;
    }

    @Override
    public void attach(@Nonnull IComputerAccess computer) {
        this.computer = computer;
        entity.subscribe(this);
    }

    @Override
    public void detach(@Nonnull IComputerAccess computer) {
        entity.unsubscribe(this);
    }

    @SuppressWarnings("java:S1201")
    @Override
    public boolean equals(@Nullable IPeripheral other) {
        return other instanceof MeterPeripheral && entity.equals(((MeterPeripheral) other).entity);
    }

    @LuaFunction(mainThread = true)
    public final MethodResult getInterval() {
        return MethodResult.of(entity.getInterval());
    }

    @LuaFunction(mainThread = true)
    public final MethodResult getTransferRate() {
        return MethodResult.of(entity.getTransferRate());
    }

    @LuaFunction(mainThread = true)
    public final MethodResult getThreshold() {
        return MethodResult.of(entity.getThreshold());
    }

    @LuaFunction(mainThread = true)
    public final MethodResult getNumberMode() {
        return MethodResult.of(entity.getNumberMode().name());
    }

    @LuaFunction(mainThread = true)
    public final MethodResult getMode() {
        return MethodResult.of(entity.getMode().name());
    }

    @LuaFunction(mainThread = true)
    public final MethodResult getAccuracy() {
        return MethodResult.of(entity.getAccuracy().name());
    }

    @LuaFunction(mainThread = true)
    public final MethodResult getStatus() {
        return MethodResult.of(entity.getStatus().name());
    }

    @LuaFunction(mainThread = true)
    public final MethodResult getFullSideConfig() {
        return MethodResult.of(entity.getSideConfig().asStringMap());
    }
}
