package com.almostreliable.energymeter;

import com.almostreliable.energymeter.core.Config;
import com.almostreliable.energymeter.core.Registration;
import com.almostreliable.energymeter.compat.CapabilityAdapterFactory;
import com.almostreliable.energymeter.data.DataGeneration;
import com.almostreliable.energymeter.network.PacketHandler;

import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.LoadingModList;

import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

@Mod(ModConstants.MOD_ID)
public final class EnergyMeter {

    public static final Logger LOGGER = LogUtils.getLogger();

    public EnergyMeter(IEventBus modEventBus, ModContainer modContainer) {
        Registration.init(modEventBus);
        CapabilityAdapterFactory.init(modEventBus);
        PacketHandler.init(modEventBus);
        Config.init(modContainer);
        modEventBus.addListener(DataGeneration::init);
    }

    public static ResourceLocation getRL(String key) {
        return ResourceLocation.fromNamespaceAndPath(ModConstants.MOD_ID, key);
    }

    public static boolean isModLoaded(String modId) {
        var modList = ModList.get();
        if (modList == null) {
            return LoadingModList.get().getModFileById(modId) != null;
        }
        return modList.isLoaded(modId);
    }
}
