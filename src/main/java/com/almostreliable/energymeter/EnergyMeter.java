package com.almostreliable.energymeter;

import com.almostreliable.energymeter.compat.CompatInitializer;
import com.almostreliable.energymeter.core.Config;
import com.almostreliable.energymeter.core.ModRegistration;
import com.almostreliable.energymeter.data.DataGeneration;
import com.almostreliable.energymeter.network.PacketHandler;

import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLLoader;

import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

@Mod(ModConstants.MOD_ID)
public final class EnergyMeter {

    public static final Logger LOGGER = LogUtils.getLogger();

    public EnergyMeter(IEventBus modEventBus, ModContainer modContainer) {
        ModRegistration.init(modEventBus);
        PacketHandler.init(modEventBus);
        Config.init(modContainer);
        CompatInitializer.init(modEventBus);
        modEventBus.addListener(DataGeneration::initClient);
        modEventBus.addListener(DataGeneration::initServer);
    }

    public static Identifier getRL(String key) {
        return Identifier.fromNamespaceAndPath(ModConstants.MOD_ID, key);
    }

    public static boolean isModLoaded(String modId) {
        var modList = ModList.get();
        if (modList == null) {
            return FMLLoader.getCurrent().getLoadingModList().getModFileById(modId) != null;
        }
        return modList.isLoaded(modId);
    }
}
