package testmod;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

import testmod.content.EnergyEmitterBlockScreen;

@Mod(value = TestMod.MOD_ID, dist = Dist.CLIENT)
public class TestModClient {

    public TestModClient(IEventBus modEventBus) {
        modEventBus.addListener(this::registerScreens);
    }

    private void registerScreens(RegisterMenuScreensEvent event) {
        event.register(TestRegistration.ENERGY_EMITTER_BLOCK_MENU.get(), EnergyEmitterBlockScreen::new);
    }
}
