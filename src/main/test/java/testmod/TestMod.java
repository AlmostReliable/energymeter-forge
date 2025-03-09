package java.testmod;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;

@Mod(TestMod.MOD_ID)
public final class TestMod {

    public static final String MOD_ID = "testmod";

    public TestMod(IEventBus modEventBus) {
        Registration.init(modEventBus);
    }
}
