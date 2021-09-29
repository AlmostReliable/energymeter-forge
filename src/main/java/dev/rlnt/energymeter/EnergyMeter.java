package dev.rlnt.energymeter;

import static dev.rlnt.energymeter.core.Constants.MOD_ID;

import dev.rlnt.energymeter.client.MeterRenderer;
import dev.rlnt.energymeter.client.gui.MeterScreen;
import dev.rlnt.energymeter.core.Setup;
import dev.rlnt.energymeter.network.PacketHandler;
import net.minecraft.client.gui.ScreenManager;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.client.registry.ClientRegistry;
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
        modEventBus.addListener(EnergyMeter::setup);
        // register client listener
        modEventBus.addListener(EnergyMeter::clientSetup);
        // register mod contents
        Setup.init(modEventBus);
    }

    private static void setup(final FMLCommonSetupEvent event) {
        // initialize packet handler
        PacketHandler.init();
    }

    private static void clientSetup(final FMLClientSetupEvent event) {
        // register screens (GUIs)
        ScreenManager.register(Setup.Containers.METER_CONTAINER.get(), MeterScreen::new);
        // register renderers
        ClientRegistry.bindTileEntityRenderer(Setup.Tiles.METER_TILE.get(), MeterRenderer::new);
    }
}
