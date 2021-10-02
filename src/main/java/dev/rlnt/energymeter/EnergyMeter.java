package dev.rlnt.energymeter;

import static dev.rlnt.energymeter.core.Constants.MOD_ID;

import dev.rlnt.energymeter.client.MeterRenderer;
import dev.rlnt.energymeter.client.gui.MeterScreen;
import dev.rlnt.energymeter.core.Setup;
import dev.rlnt.energymeter.network.PacketHandler;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(MOD_ID)
@Mod.EventBusSubscriber
public class EnergyMeter {

    @SuppressWarnings("java:S1118")
    public EnergyMeter() {
        final IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        // register setup listener
        modEventBus.addListener(EnergyMeter::onCommonSetup);
        // register client listener
        modEventBus.addListener(EnergyMeter::onClientSetup);
        // register mod contents
        Setup.init(modEventBus);
    }

    private static void onCommonSetup(final FMLCommonSetupEvent event) {
        // initialize packet handler
        PacketHandler.init();
    }

    private static void onClientSetup(final FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            // register screens (GUIs)
            MenuScreens.register(Setup.Containers.METER_CONTAINER.get(), MeterScreen::new);
            // register renderers
            BlockEntityRenderers.register(Setup.Entities.METER_ENTITY.get(), MeterRenderer::new);
        });
    }
}
