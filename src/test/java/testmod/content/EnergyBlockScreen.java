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

public class EnergyBlockScreen extends SynchronizedContainerScreen<EnergyBlockMenu> {

    public EnergyBlockScreen(EnergyBlockMenu menu, Inventory playerInventory, Component title) {
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
        layout.addChild(new InputLayoutElement<>(
            TextBoxType.CAPACITY,
            110,
            font,
            Component.literal("Capacity:"),
            () -> String.valueOf(menu.getCapacity()),
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

    public enum TextBoxType implements TextValueClientAction.ValueConsumer<EnergyBlockEntity> {
        ENERGY_TO_EMIT(EnergyBlockEntity::setEnergyToEmitPerTick),
        CAPACITY(EnergyBlockEntity::setCapacity);

        private final BiConsumer<EnergyBlockEntity, Integer> valueUpdater;

        TextBoxType(BiConsumer<EnergyBlockEntity, Integer> valueUpdater) {
            this.valueUpdater = valueUpdater;
        }

        @Override
        public void updateValue(EnergyBlockEntity blockEntity, int value) {
            valueUpdater.accept(blockEntity, value);
        }
    }
}
