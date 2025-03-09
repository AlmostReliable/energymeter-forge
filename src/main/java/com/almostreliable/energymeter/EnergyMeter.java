package com.almostreliable.energymeter;

import com.almostreliable.energymeter.core.Config;
import com.almostreliable.energymeter.core.Registration;
import com.almostreliable.energymeter.data.DataGeneration;
import com.almostreliable.energymeter.network.PacketHandler;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;

import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

@Mod(ModConstants.MOD_ID)
public final class EnergyMeter {

    public static final Logger LOGGER = LogUtils.getLogger();

    public EnergyMeter(IEventBus modEventBus, ModContainer modContainer) {
        Registration.init(modEventBus);
        PacketHandler.init(modEventBus);
        Config.init(modContainer);
        modEventBus.addListener(DataGeneration::init);
    }
}
