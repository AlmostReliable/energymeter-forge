package com.almostreliable.energymeter.compat.cct;

import com.almostreliable.energymeter.ModConstants;
import com.almostreliable.energymeter.block.component.IoConfig.IoSettingWithPriority;
import com.almostreliable.energymeter.block.entity.MeterBlockEntity;
import com.almostreliable.energymeter.compat.IMeterEntityObserver;

import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.lua.LuaFunction;
import dan200.computercraft.api.lua.MethodResult;
import dan200.computercraft.api.peripheral.IComputerAccess;
import dan200.computercraft.api.peripheral.IPeripheral;
import net.minecraft.core.Direction;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

@SuppressWarnings({"unused", "FinalMethod"})
public class MeterPeripheral implements IPeripheral, IMeterEntityObserver {

    private static final Set<MeterPeripheral> ATTACHED_PERIPHERALS = Collections.newSetFromMap(new IdentityHashMap<>());

    private final MeterBlockEntity entity;
    private final Set<IComputerAccess> computers = new HashSet<>();
    private Map<String, Object> lastData = Map.of();

    MeterPeripheral(MeterBlockEntity entity) {
        this.entity = entity;
    }

    @Override
    public void onMeterTileChanged(MeterBlockEntity entity, int flags) {
        if (this.entity.equals(entity)) {
            queueEvent("em_data_changed", createData());
        }
    }

    @Override
    public void onMeterTileRemoved(MeterBlockEntity entity) {
        if (this.entity.equals(entity)) {
            queueEvent("em_removed");
        }
    }

    @Nonnull
    @Override
    public String getType() {
        return ModConstants.MOD_ID;
    }

    @Override
    public void attach(@Nonnull IComputerAccess computer) {
        computers.add(computer);
        lastData = createData();
        ATTACHED_PERIPHERALS.add(this);
    }

    @Override
    public void detach(@Nonnull IComputerAccess computer) {
        computers.remove(computer);
        if (computers.isEmpty()) {
            ATTACHED_PERIPHERALS.remove(this);
        }
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
    public final MethodResult getEnergyRate() {
        return MethodResult.of(entity.getEnergyRate());
    }

    @LuaFunction(mainThread = true)
    public final MethodResult getThreshold() {
        return MethodResult.of(entity.getZeroTolerance());
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
    public final MethodResult getTotalEnergy() {
        return MethodResult.of(entity.getTotalEnergy());
    }

    @LuaFunction(mainThread = true)
    public final MethodResult getTransferLimit() {
        return MethodResult.of(entity.getTransferLimit());
    }

    @LuaFunction(mainThread = true)
    public final MethodResult hasInput() {
        return MethodResult.of(entity.getIoConfig().hasInput());
    }

    @LuaFunction(mainThread = true)
    public final MethodResult hasOutput() {
        return MethodResult.of(entity.getIoConfig().hasOutput());
    }

    @LuaFunction(mainThread = true)
    public final MethodResult getSideConfig(String side) throws LuaException {
        IoSettingWithPriority setting = entity.getIoConfig().getSetting(parseDirection(side));
        return MethodResult.of(setting.setting().name());
    }

    @LuaFunction(mainThread = true)
    public final MethodResult getSideConfigDetails(String side) throws LuaException {
        return MethodResult.of(createSideData(parseDirection(side)));
    }

    @LuaFunction(mainThread = true)
    public final MethodResult getFullSideConfig() {
        return MethodResult.of(createSideConfigData());
    }

    @LuaFunction(mainThread = true)
    public final MethodResult getFullSideConfigDetails() {
        return MethodResult.of(createDetailedSideConfigData());
    }

    @LuaFunction(mainThread = true)
    public final MethodResult getData() {
        return MethodResult.of(createData());
    }

    private Map<String, Object> createData() {
        Map<String, Object> data = new HashMap<>();
        data.put("side_config", createSideConfigData());
        data.put("side_config_details", createDetailedSideConfigData());
        data.put("transfer_rate", entity.getEnergyRate());
        data.put("energy_rate", entity.getEnergyRate());
        data.put("total_energy", entity.getTotalEnergy());
        data.put("status", entity.getConnectionStatus().name());
        data.put("mode", entity.getTransferMode().name());
        data.put("transfer_mode", entity.getTransferMode().name());
        data.put("accuracy", entity.getMeasureMode().name());
        data.put("measure_mode", entity.getMeasureMode().name());
        data.put("interval", entity.getMeasureInterval());
        data.put("measure_interval", entity.getMeasureInterval());
        data.put("threshold", entity.getZeroTolerance());
        data.put("zero_tolerance", entity.getZeroTolerance());
        data.put("transfer_limit", entity.getTransferLimit());
        return data;
    }

    private void tick() {
        if (entity.isRemoved()) {
            queueEvent("em_removed");
            computers.clear();
            ATTACHED_PERIPHERALS.remove(this);
            return;
        }

        Map<String, Object> data = createData();
        if (!data.equals(lastData)) {
            lastData = data;
            queueEvent("em_data_changed", data);
        }
    }

    private void queueEvent(String event, Object... arguments) {
        for (IComputerAccess computer : computers) {
            computer.queueEvent(event, arguments);
        }
    }

    public static void tickAttachedPeripherals() {
        Set.copyOf(ATTACHED_PERIPHERALS).forEach(MeterPeripheral::tick);
    }

    private Map<String, Object> createSideConfigData() {
        Map<String, Object> data = new HashMap<>();
        for (Direction direction : Direction.values()) {
            IoSettingWithPriority setting = entity.getIoConfig().getSetting(direction);
            data.put(direction.getName(), setting.setting().name());
        }
        return data;
    }

    private Map<String, Object> createDetailedSideConfigData() {
        Map<String, Object> data = new HashMap<>();
        for (Direction direction : Direction.values()) {
            data.put(direction.getName(), createSideData(direction));
        }
        return data;
    }

    private Map<String, Object> createSideData(Direction direction) {
        IoSettingWithPriority setting = entity.getIoConfig().getSetting(direction);
        Map<String, Object> data = new HashMap<>();
        data.put("setting", setting.setting().name());
        data.put("priority", setting.priority());
        return data;
    }

    private static Direction parseDirection(String side) throws LuaException {
        Direction direction = Direction.byName(side.toLowerCase(Locale.ROOT));
        if (direction == null) {
            throw new LuaException("Invalid side: " + side);
        }
        return direction;
    }
}
