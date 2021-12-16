package com.github.almostreliable.energymeter;

import com.github.almostreliable.energymeter.client.MeterRenderer;
import com.github.almostreliable.energymeter.client.gui.MeterScreen;
import com.github.almostreliable.energymeter.core.Setup;
import com.github.almostreliable.energymeter.core.Setup.Containers;
import com.github.almostreliable.energymeter.core.Setup.Tiles;
import com.github.almostreliable.energymeter.network.PacketHandler;
import net.minecraft.client.gui.ScreenManager;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import static com.github.almostreliable.energymeter.core.Constants.MOD_ID;

@Mod(MOD_ID)
@EventBusSubscriber
public class EnergyMeter {

    @SuppressWarnings("java:S1118")
    public EnergyMeter() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        // register common setup listener
        modEventBus.addListener(EnergyMeter::onCommonSetup);
        // register client listener
        modEventBus.addListener(EnergyMeter::onClientSetup);
        // register mod contents
        Setup.init(modEventBus);
    }

    private static void onCommonSetup(FMLCommonSetupEvent event) {
        // initialize packet handler
        PacketHandler.init();
    }

    private static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            // register screens
            ScreenManager.register(Containers.METER_CONTAINER.get(), MeterScreen::new);
            // register renderers
            ClientRegistry.bindTileEntityRenderer(Tiles.METER.get(), MeterRenderer::new);
        });
    }
}