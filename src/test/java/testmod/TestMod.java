package testmod;

import com.almostreliable.energymeter.network.action.ClientActionRegistry;
import com.almostreliable.energymeter.network.action.TextValueClientAction;

import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;

import testmod.content.EnergyEmitterBlockEntity;
import testmod.content.EnergyEmitterBlockMenu;
import testmod.content.EnergyEmitterBlockScreen;

@Mod(TestMod.MOD_ID)
public final class TestMod {

    public static final String MOD_ID = "testmod";
    public static final ResourceLocation UPDATE_TEXT_ID = ResourceLocation.fromNamespaceAndPath(MOD_ID, "update_text");

    public TestMod(IEventBus modEventBus) {
        TestRegistration.init(modEventBus);
        ClientActionRegistry.register(
            UPDATE_TEXT_ID,
            TextValueClientAction.<EnergyEmitterBlockEntity, EnergyEmitterBlockMenu, EnergyEmitterBlockScreen.TextBoxType> decoder(
                UPDATE_TEXT_ID,
                EnergyEmitterBlockScreen.TextBoxType.class
            )
        );
    }
}
