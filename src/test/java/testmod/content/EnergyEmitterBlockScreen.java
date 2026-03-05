package testmod.content;

import com.almostreliable.energymeter.client.screen.SynchronizedContainerScreen;
import com.almostreliable.energymeter.client.screen.layout.InputLayoutElement;
import com.almostreliable.energymeter.network.action.TextValueClientAction;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

import testmod.TestMod;

public class EnergyEmitterBlockScreen extends SynchronizedContainerScreen<EnergyEmitterBlockMenu> {

    public EnergyEmitterBlockScreen(EnergyEmitterBlockMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
    }

    @Override
    protected void init() {
        super.init();

        var layout = LinearLayout.vertical().spacing(2);
        layout.addChild(new InputLayoutElement<>(
            EnergyMitterTextBoxType.ENERGY_TO_EMIT,
            110,
            font,
            Component.literal("Energy to emit:"),
            () -> String.valueOf(menu.getEnergyToEmitPerTick()),
            this::onTextValueUpdated
        ).withMaxValue(Integer.MAX_VALUE));

        layout.setPosition(leftPos, topPos);
        layout.arrangeElements();
        layout.visitWidgets(this::addRenderableWidget);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        // no-op
    }

    private void onTextValueUpdated(EnergyMitterTextBoxType textBox, long value) {
        sendClientAction(new TextValueClientAction<>(TestMod.UPDATE_TEXT_ID, textBox, value));
    }
}
