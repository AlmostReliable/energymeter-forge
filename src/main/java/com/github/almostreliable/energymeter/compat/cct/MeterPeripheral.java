package com.github.almostreliable.energymeter.compat.cct;

import com.github.almostreliable.energymeter.compat.IMeterTileObserver;
import com.github.almostreliable.energymeter.meter.MeterTile;
import dan200.computercraft.api.lua.LuaFunction;
import dan200.computercraft.api.lua.MethodResult;
import dan200.computercraft.api.peripheral.IComputerAccess;
import dan200.computercraft.api.peripheral.IPeripheral;
import net.minecraft.util.Direction;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

import static com.github.almostreliable.energymeter.core.Constants.*;

@SuppressWarnings({"unused", "FinalMethod"})
public class MeterPeripheral implements IPeripheral, IMeterTileObserver {

    private final MeterTile tile;
    private IComputerAccess computer;

    MeterPeripheral(MeterTile tile) {
        this.tile = tile;
    }

    @LuaFunction(mainThread = true)
    public final MethodResult getSideConfig(Direction direction) {
        return MethodResult.of(tile.getSideConfig().get(direction).name());
    }

    @LuaFunction(mainThread = true)
    public final MethodResult hasInput() {
        return MethodResult.of(tile.getSideConfig().hasInput());
    }

    @LuaFunction(mainThread = true)
    public final MethodResult hasOutput() {
        return MethodResult.of(tile.getSideConfig().hasOutput());
    }

    @LuaFunction(mainThread = true)
    public final MethodResult hasMaxOutputs() {
        return MethodResult.of(tile.getSideConfig().hasMaxOutputs());
    }

    @Override
    public void onMeterTileChanged(MeterTile tile, int flags) {
        if (!this.tile.equals(tile)) return;

        Map<String, Object> data = new HashMap<>();
        if ((flags & SYNC_FLAGS.SIDE_CONFIG) != 0) data.put(SIDE_CONFIG_ID, tile.getSideConfig().asStringMap());
        if ((flags & SYNC_FLAGS.TRANSFER_RATE) != 0) data.put(TRANSFER_RATE_ID, tile.getTransferRate());
        if ((flags & SYNC_FLAGS.NUMBER_MODE) != 0) data.put(NUMBER_MODE_ID, tile.getNumberMode().name());
        if ((flags & SYNC_FLAGS.STATUS) != 0) data.put(STATUS_ID, tile.getStatus().name());
        if ((flags & SYNC_FLAGS.MODE) != 0) data.put(MODE_ID, tile.getMode().name());
        if ((flags & SYNC_FLAGS.ACCURACY) != 0) data.put(ACCURACY_ID, tile.getAccuracy().name());
        if ((flags & SYNC_FLAGS.INTERVAL) != 0) data.put(INTERVAL_ID, tile.getInterval());
        if ((flags & SYNC_FLAGS.THRESHOLD) != 0) data.put(THRESHOLD_ID, tile.getThreshold());
        computer.queueEvent("em_data_changed", data);
    }

    @Override
    public void onMeterTileRemoved(MeterTile tile) {
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
        tile.subscribe(this);
    }

    @Override
    public void detach(@Nonnull IComputerAccess computer) {
        tile.unsubscribe(this);
    }

    @SuppressWarnings("java:S1201")
    @Override
    public boolean equals(@Nullable IPeripheral other) {
        return other instanceof MeterPeripheral && tile.equals(((MeterPeripheral) other).tile);
    }

    @LuaFunction(mainThread = true)
    public final MethodResult getInterval() {
        return MethodResult.of(tile.getInterval());
    }

    @LuaFunction(mainThread = true)
    public final MethodResult getTransferRate() {
        return MethodResult.of(tile.getTransferRate());
    }

    @LuaFunction(mainThread = true)
    public final MethodResult getThreshold() {
        return MethodResult.of(tile.getThreshold());
    }

    @LuaFunction(mainThread = true)
    public final MethodResult getNumberMode() {
        return MethodResult.of(tile.getNumberMode().name());
    }

    @LuaFunction(mainThread = true)
    public final MethodResult getMode() {
        return MethodResult.of(tile.getMode().name());
    }

    @LuaFunction(mainThread = true)
    public final MethodResult getAccuracy() {
        return MethodResult.of(tile.getAccuracy().name());
    }

    @LuaFunction(mainThread = true)
    public final MethodResult getStatus() {
        return MethodResult.of(tile.getStatus().name());
    }

    @LuaFunction(mainThread = true)
    public final MethodResult getFullSideConfig() {
        return MethodResult.of(tile.getSideConfig().asStringMap());
    }
}
