package com.github.almostreliable.energymeter.compat.cct;

import com.github.almostreliable.energymeter.meter.MeterTile;
import dan200.computercraft.api.lua.LuaFunction;
import dan200.computercraft.api.lua.MethodResult;
import dan200.computercraft.api.peripheral.IComputerAccess;
import dan200.computercraft.api.peripheral.IPeripheral;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class MeterPeripheral implements IPeripheral {

    public static final String EM_TYPE = "energymeter";
    private static final Logger LOGGER = LogManager.getLogger();

    private final MeterTile meterTile;

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
    public MethodResult getFoo() {
        return MethodResult.of("yeet");
    }

    @Override
    public void attach(@Nonnull IComputerAccess computer) {
        IPeripheral.super.attach(computer);
        LOGGER.info("ATTACHED TO " + computer);
    }

    @Override
    public void detach(@Nonnull IComputerAccess computer) {
        IPeripheral.super.detach(computer);
        LOGGER.info("DETTACHED TO " + computer);
    }
}
