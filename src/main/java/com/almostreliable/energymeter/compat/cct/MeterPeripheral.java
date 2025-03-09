package com.almostreliable.energymeter.compat.cct;

import com.almostreliable.energymeter.ModConstants;
import com.almostreliable.energymeter.block.entity.MeterBlockEntity;
import com.almostreliable.energymeter.compat.IMeterEntityObserver;

import net.minecraft.core.Direction;

import dan200.computercraft.api.lua.LuaFunction;
import dan200.computercraft.api.lua.MethodResult;
import dan200.computercraft.api.peripheral.IComputerAccess;
import dan200.computercraft.api.peripheral.IPeripheral;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

import static com.almostreliable.energymeter.core.Constants.DISPLAY_MODE_ID;
import static com.almostreliable.energymeter.core.Constants.MEASURE_INTERVAL_ID;
import static com.almostreliable.energymeter.core.Constants.MEASURE_MODE_ID;
import static com.almostreliable.energymeter.core.Constants.SIDE_CONFIG_ID;
import static com.almostreliable.energymeter.core.Constants.STATUS_ID;
import static com.almostreliable.energymeter.core.Constants.SyncFlags;
import static com.almostreliable.energymeter.core.Constants.TRANSFER_MODE_ID;
import static com.almostreliable.energymeter.core.Constants.TRANSFER_RATE_ID;
import static com.almostreliable.energymeter.core.Constants.ZERO_TOLERANCE_ID;

@SuppressWarnings({"unused", "FinalMethod"})
public class MeterPeripheral implements IPeripheral, IMeterEntityObserver {

    private final MeterBlockEntity entity;
    private IComputerAccess computer;

    MeterPeripheral(MeterBlockEntity entity) {
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
    public void onMeterTileChanged(MeterBlockEntity entity, int flags) {
        if (!this.entity.equals(entity)) return;

        Map<String, Object> data = new HashMap<>();
        if ((flags & SyncFlags.SIDE_CONFIG) != 0) data.put(SIDE_CONFIG_ID, entity.getSideConfig().asStringMap());
        if ((flags & SyncFlags.TRANSFER_RATE) != 0) data.put(TRANSFER_RATE_ID, entity.getEnergyRate());
        if ((flags & SyncFlags.NUMBER_MODE) != 0) data.put(DISPLAY_MODE_ID, entity.getDisplayMode().name());
        if ((flags & SyncFlags.STATUS) != 0) data.put(STATUS_ID, entity.getConnectionStatus().name());
        if ((flags & SyncFlags.MODE) != 0) data.put(TRANSFER_MODE_ID, entity.getTransferMode().name());
        if ((flags & SyncFlags.ACCURACY) != 0) data.put(MEASURE_MODE_ID, entity.getMeasureMode().name());
        if ((flags & SyncFlags.INTERVAL) != 0) data.put(MEASURE_INTERVAL_ID, entity.getMeasureInterval());
        if ((flags & SyncFlags.THRESHOLD) != 0) data.put(ZERO_TOLERANCE_ID, entity.getZeroTolerance());
        computer.queueEvent("em_data_changed", data);
    }

    @Override
    public void onMeterTileRemoved(MeterBlockEntity entity) {
        computer.queueEvent("em_removed");
    }

    @Nonnull
    @Override
    public String getType() {
        return ModConstants.MOD_ID;
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
        return MethodResult.of(entity.getMeasureInterval());
    }

    @LuaFunction(mainThread = true)
    public final MethodResult getTransferRate() {
        return MethodResult.of(entity.getEnergyRate());
    }

    @LuaFunction(mainThread = true)
    public final MethodResult getThreshold() {
        return MethodResult.of(entity.getZeroTolerance());
    }

    @LuaFunction(mainThread = true)
    public final MethodResult getNumberMode() {
        return MethodResult.of(entity.getDisplayMode().name());
    }

    @LuaFunction(mainThread = true)
    public final MethodResult getMode() {
        return MethodResult.of(entity.getTransferMode().name());
    }

    @LuaFunction(mainThread = true)
    public final MethodResult getAccuracy() {
        return MethodResult.of(entity.getMeasureMode().name());
    }

    @LuaFunction(mainThread = true)
    public final MethodResult getStatus() {
        return MethodResult.of(entity.getConnectionStatus().name());
    }

    @LuaFunction(mainThread = true)
    public final MethodResult getFullSideConfig() {
        return MethodResult.of(entity.getSideConfig().asStringMap());
    }
}
