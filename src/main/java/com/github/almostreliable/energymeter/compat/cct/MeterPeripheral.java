package com.github.almostreliable.energymeter.compat.cct;

import com.github.almostreliable.energymeter.component.SideConfiguration;
import com.github.almostreliable.energymeter.core.Constants.SYNC_FLAGS;
import com.github.almostreliable.energymeter.meter.IMeterTileObserver;
import com.github.almostreliable.energymeter.meter.MeterTile;
import dan200.computercraft.api.lua.LuaFunction;
import dan200.computercraft.api.lua.MethodResult;
import dan200.computercraft.api.peripheral.IComputerAccess;
import dan200.computercraft.api.peripheral.IPeripheral;
import net.minecraft.util.Direction;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

public class MeterPeripheral implements IPeripheral, IMeterTileObserver {

    public static final String EM_TYPE = "energymeter";
    private static final Logger LOGGER = LogManager.getLogger();

    private final MeterTile meterTile;
    private IComputerAccess computer;

    public MeterPeripheral(MeterTile meterTile) {
        this.meterTile = meterTile;
    }

    @Nonnull
    @Override
    public String getType() {
        return EM_TYPE;
    }

    @Override
    public boolean equals(@Nullable IPeripheral other) {
        if (other instanceof MeterPeripheral) {
            return meterTile.equals(((MeterPeripheral) other).meterTile);
        }

        return false;
    }

    @LuaFunction(mainThread = true)
    public final MethodResult getInterval() {
        return MethodResult.of(meterTile.getInterval());
    }

    @LuaFunction(mainThread = true)
    public final MethodResult getTransferRate() {
        return MethodResult.of(meterTile.getTransferRate());
    }

    @LuaFunction(mainThread = true)
    public final MethodResult getThreshold() {
        return MethodResult.of(meterTile.getThreshold());
    }

    @LuaFunction(mainThread = true)
    public final MethodResult getNumberMode() {
        return MethodResult.of(meterTile.getNumberMode().name());
    }

    @LuaFunction(mainThread = true)
    public final MethodResult getMode() {
        return MethodResult.of(meterTile.getMode().name());
    }
    
    @LuaFunction(mainThread = true)
    public final MethodResult getAccuracy() {
        return MethodResult.of(meterTile.getAccuracy().name());
    }

    @LuaFunction(mainThread = true)
    public final MethodResult getStatus() {
        return MethodResult.of(meterTile.getStatus().name());
    }

    @LuaFunction(mainThread = true)
    public final MethodResult getSideConfig(Direction direction) {
        return MethodResult.of(meterTile.getSideConfig().get(direction).name());
    }

    @LuaFunction(mainThread = true)
    public final MethodResult getFullSideConfig() {
        return MethodResult.of(sideConfigToMap(meterTile.getSideConfig()));
    }

    @LuaFunction(mainThread = true)
    public final MethodResult hasInput() {
        return MethodResult.of(meterTile.getSideConfig().hasInput());
    }

    @LuaFunction(mainThread = true)
    public final MethodResult hasOutput() {
        return MethodResult.of(meterTile.getSideConfig().hasOutput());
    }

    @LuaFunction(mainThread = true)
    public final MethodResult hasMaxOutputs() {
        return MethodResult.of(meterTile.getSideConfig().hasMaxOutputs());
    }

    @Override
    public void attach(@Nonnull IComputerAccess computer) {
        this.computer = computer;
        meterTile.subscribe(this);
    }

    @Override
    public void detach(@Nonnull IComputerAccess computer) {
        meterTile.unsubscribe(this);
    }

    @Override
    public void onMeterTileChanged(MeterTile tile, int flags) {
        if (meterTile.equals(tile)) {
            Map<String, Object> data = new HashMap<>();
            if ((flags & SYNC_FLAGS.SIDE_CONFIG) != 0) data.put("side_config", sideConfigToMap(tile.getSideConfig()));
            if ((flags & SYNC_FLAGS.TRANSFER_RATE) != 0) data.put("transfer_rate", tile.getTransferRate());
            if ((flags & SYNC_FLAGS.NUMBER_MODE) != 0) data.put("number_mode", tile.getNumberMode().name());
            if ((flags & SYNC_FLAGS.STATUS) != 0) data.put("status", tile.getStatus().name());
            if ((flags & SYNC_FLAGS.MODE) != 0) data.put("mode", tile.getMode().name());
            if ((flags & SYNC_FLAGS.ACCURACY) != 0) data.put("accuracy", tile.getAccuracy().name());
            if ((flags & SYNC_FLAGS.INTERVAL) != 0) data.put("interval", tile.getInterval());
            if ((flags & SYNC_FLAGS.THRESHOLD) != 0) data.put("threshold", tile.getThreshold());
            computer.queueEvent("em_data_changed", data);
        }
    }

    @Override
    public void onMeterTileRemoved(MeterTile tile) {
        computer.queueEvent("em_removed");
    }

    private Map<String, String> sideConfigToMap(SideConfiguration sideConfig) {
        Map<String, String> config = new HashMap<>();
        for (Direction value : Direction.values()) {
            config.put(value.getName(), sideConfig.get(value).name());
        }
        return config;
    }
}
