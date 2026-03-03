package testmod.content;

import com.almostreliable.energymeter.client.screen.SynchronizedContainerScreen;
import com.almostreliable.energymeter.client.screen.layout.InputLayoutElement;
import com.almostreliable.energymeter.network.action.TextValueClientAction;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

import testmod.TestMod;

import java.util.function.BiConsumer;

public class EnergyEmitterBlockScreen extends SynchronizedContainerScreen<EnergyEmitterBlockMenu> {

    public EnergyEmitterBlockScreen(EnergyEmitterBlockMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
    }

    @Override
    protected void init() {
        super.init();

        var layout = LinearLayout.vertical().spacing(2);
        layout.addChild(new InputLayoutElement<>(
            TextBoxType.ENERGY_TO_EMIT,
            110,
            font,
            Component.literal("Energy to emit:"),
            () -> String.valueOf(menu.getEnergyToEmitPerTick()),
            this::onTextValueUpdated
        ));

        layout.setPosition(leftPos, topPos);
        layout.arrangeElements();
        layout.visitWidgets(this::addRenderableWidget);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        // no-op
    }

    private void onTextValueUpdated(TextBoxType textBox, int value) {
        sendClientAction(new TextValueClientAction<>(TestMod.UPDATE_TEXT_ID, textBox, value));
    }

    public enum TextBoxType implements TextValueClientAction.ValueConsumer<EnergyEmitterBlockEntity> {
        ENERGY_TO_EMIT(EnergyEmitterBlockEntity::setEnergyToEmitPerTick);

        private final BiConsumer<EnergyEmitterBlockEntity, Integer> valueUpdater;

        TextBoxType(BiConsumer<EnergyEmitterBlockEntity, Integer> valueUpdater) {
            this.valueUpdater = valueUpdater;
        }

        @Override
        public void updateValue(EnergyEmitterBlockEntity blockEntity, int value) {
            valueUpdater.accept(blockEntity, value);
        }
    }
}
