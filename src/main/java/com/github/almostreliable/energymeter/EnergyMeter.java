package com.github.almostreliable.energymeter;

import com.almostreliable.energymeter.EnergyMeterConstants;
import com.github.almostreliable.energymeter.client.MeterRenderer;
import com.github.almostreliable.energymeter.client.gui.MeterScreen;
import com.github.almostreliable.energymeter.core.Setup;
import com.github.almostreliable.energymeter.core.Setup.Containers;
import com.github.almostreliable.energymeter.core.Setup.Entities;
import com.github.almostreliable.energymeter.network.PacketHandler;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.RegisterEvent;

@Mod(EnergyMeterConstants.MOD_ID)
public class EnergyMeter {

    public EnergyMeter() {
        var modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        // register common setup listener
        modEventBus.addListener(EnergyMeter::onCommonSetup);
        // register registry event listener
        modEventBus.addListener(EnergyMeter::onRegistryEvent);
        // register creative tab contents listener
        modEventBus.addListener(EnergyMeter::onCreativeTabContents);
        // register client setup listener
        modEventBus.addListener(EnergyMeter::onClientSetup);
        // register mod contents
        Setup.init(modEventBus);
    }

    private static void onCommonSetup(FMLCommonSetupEvent event) {
        // initialize packet handler
        PacketHandler.init();
    }

    private static void onRegistryEvent(RegisterEvent event) {
        // register creative tab
        Setup.Tab.registerTab(event);
    }

    private static void onCreativeTabContents(BuildCreativeModeTabContentsEvent event) {
        // add creative tab contents
        Setup.Tab.initContents(event);
    }

    private static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            // register screens
            MenuScreens.register(Containers.METER.get(), MeterScreen::new);
            // register renderers
            BlockEntityRenderers.register(Entities.METER.get(), MeterRenderer::new);
        });
    }
}
