package testmod;

import com.almostreliable.energymeter.network.action.ClientActionRegistry;
import com.almostreliable.energymeter.network.action.TextValueClientAction;

import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;

import testmod.content.EnergyEmitterBlockEntity;
import testmod.content.EnergyEmitterBlockMenu;
import testmod.content.EnergyMitterTextBoxType;

@Mod(TestMod.MOD_ID)
public final class TestMod {

    public static final String MOD_ID = "testmod";
    public static final Identifier UPDATE_TEXT_ID = Identifier.fromNamespaceAndPath(MOD_ID, "update_text");

    public TestMod(IEventBus modEventBus) {
        TestRegistration.init(modEventBus);
        modEventBus.addListener(TestFunctions::registerTests);
        ClientActionRegistry.register(
            UPDATE_TEXT_ID,
            TextValueClientAction.<EnergyEmitterBlockEntity, EnergyEmitterBlockMenu, EnergyMitterTextBoxType> decoder(
                UPDATE_TEXT_ID,
                EnergyMitterTextBoxType.class
            )
        );
    }
}
