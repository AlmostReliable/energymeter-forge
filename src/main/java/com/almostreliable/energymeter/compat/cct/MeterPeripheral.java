package com.almostreliable.energymeter.compat.cct;

import com.almostreliable.energymeter.ModConstants;
import com.almostreliable.energymeter.block.entity.MeterBlockEntity;
import com.almostreliable.energymeter.compat.MeterObserver;

import net.minecraft.core.Direction;

import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.lua.LuaFunction;
import dan200.computercraft.api.lua.MethodResult;
import dan200.computercraft.api.peripheral.IComputerAccess;
import dan200.computercraft.api.peripheral.IPeripheral;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

@SuppressWarnings({"unused", "FinalMethod"})
public class MeterPeripheral implements IPeripheral, MeterObserver {

    private static final Direction[] DIRECTIONS = Direction.values();
    private static final String TRANSFER_MODE_ID = "transfer_mode";
    private static final String MEASURE_MODE_ID = "measure_mode";
    private static final String MEASURE_INTERVAL_ID = "measure_interval";
    private static final String ZERO_TOLERANCE_ID = "zero_tolerance";
    private static final String TRANSFER_LIMIT_ID = "transfer_limit";
    private static final String ENERGY_RATE_ID = "energy_rate";
    private static final String TOTAL_ENERGY_ID = "total_energy";
    private static final String CONNECTION_STATUS_ID = "connection_status";
    private static final String HAS_INPUT_ID = "has_input";
    private static final String HAS_OUTPUT_ID = "has_output";
    private static final String IO_CONFIG_ID = "io_config";

    private final MeterBlockEntity entity;
    private final Set<IComputerAccess> computers = new HashSet<>();

    private Map<String, Object> lastData = Map.of();

    MeterPeripheral(MeterBlockEntity entity) {
        this.entity = entity;
    }

    @Override
    public String getType() {
        return ModConstants.MOD_ID;
    }

    @Override
    public void attach(IComputerAccess computer) {
        computers.add(computer);
        entity.subscribeObserver(this);
    }

    @Override
    public void detach(IComputerAccess computer) {
        computers.remove(computer);
        entity.unsubscribeObserver(this);
    }

    @Override
    public void onChange(MeterBlockEntity entity) {
        if (!this.entity.equals(entity)) return;
        var data = createData();
        lastData = data;
        queueEvent("energymeter_changed", data);
    }

    @Override
    public void onRemove(MeterBlockEntity entity) {
        if (!this.entity.equals(entity)) return;
        lastData = Map.of();
        queueEvent("energymeter_removed");
    }

    @SuppressWarnings("java:S1201")
    @Override
    public boolean equals(@Nullable IPeripheral other) {
        return other instanceof MeterPeripheral && entity.equals(((MeterPeripheral) other).entity);
    }

    @LuaFunction(mainThread = true)
    public final MethodResult getTransferMode() {
        return MethodResult.of(lastData.get(TRANSFER_MODE_ID));
    }

    @LuaFunction(mainThread = true)
    public final MethodResult getMeasureMode() {
        return MethodResult.of(lastData.get(MEASURE_MODE_ID));
    }

    @LuaFunction(mainThread = true)
    public final MethodResult getMeasureInterval() {
        return MethodResult.of(lastData.get(MEASURE_INTERVAL_ID));
    }

    @LuaFunction(mainThread = true)
    public final MethodResult getZeroTolerance() {
        return MethodResult.of(lastData.get(ZERO_TOLERANCE_ID));
    }

    @LuaFunction(mainThread = true)
    public final MethodResult getTransferLimit() {
        return MethodResult.of(lastData.get(TRANSFER_LIMIT_ID));
    }

    @LuaFunction(mainThread = true)
    public final MethodResult getEnergyRate() {
        return MethodResult.of(lastData.get(ENERGY_RATE_ID));
    }

    @LuaFunction(mainThread = true)
    public final MethodResult getTotalEnergy() {
        return MethodResult.of(lastData.get(TOTAL_ENERGY_ID));
    }

    @LuaFunction(mainThread = true)
    public final MethodResult getConnectionStatus() {
        return MethodResult.of(lastData.get(CONNECTION_STATUS_ID));
    }

    @LuaFunction(mainThread = true)
    public final MethodResult hasInput() {
        return MethodResult.of(lastData.get(HAS_INPUT_ID));
    }

    @LuaFunction(mainThread = true)
    public final MethodResult hasOutput() {
        return MethodResult.of(lastData.get(HAS_OUTPUT_ID));
    }

    @LuaFunction(mainThread = true)
    public final MethodResult getIoConfig() {
        return MethodResult.of(lastData.get(IO_CONFIG_ID));
    }

    @LuaFunction(mainThread = true)
    public final MethodResult getData() {
        return MethodResult.of(lastData);
    }

    private Map<String, Object> createData() {
        Map<String, Object> data = new HashMap<>();
        data.put(TRANSFER_MODE_ID, entity.getTransferMode().name());
        data.put(MEASURE_MODE_ID, entity.getMeasureMode().name());
        data.put(MEASURE_INTERVAL_ID, entity.getMeasureInterval());
        data.put(ZERO_TOLERANCE_ID, entity.getZeroTolerance());
        data.put(TRANSFER_LIMIT_ID, entity.getTransferLimit());
        data.put(ENERGY_RATE_ID, entity.getEnergyRate());
        data.put(TOTAL_ENERGY_ID, entity.getTotalEnergy());
        data.put(CONNECTION_STATUS_ID, entity.getConnectionStatus().name());
        data.put(HAS_INPUT_ID, entity.getIoConfig().hasInput());
        data.put(HAS_OUTPUT_ID, entity.getIoConfig().hasOutput());
        data.put(IO_CONFIG_ID, createIoConfigStringMap());
        return data;
    }

    private void queueEvent(String event, Object... arguments) {
        for (IComputerAccess computer : computers) {
            computer.queueEvent(event, arguments);
        }
    }

    private Map<String, String> createIoConfigStringMap() {
        Map<String, String> stringMap = new HashMap<>();
        for (var direction : DIRECTIONS) {
            var setting = entity.getIoConfig().getSetting(direction);
            stringMap.put(direction.name(), setting.setting().name());
        }
        return stringMap;
    }

    private static Direction parseDirection(String side) throws LuaException {
        Direction direction = Direction.byName(side.toUpperCase(Locale.ROOT));
        if (direction == null) {
            throw new LuaException("Invalid side: " + side);
        }
        return direction;
    }
}
