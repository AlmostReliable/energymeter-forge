package com.almostreliable.energymeter.client.screen.layout;

import com.almostreliable.energymeter.block.entity.MeterBlockEntity;
import com.almostreliable.energymeter.client.screen.widget.MarqueeStringWidget;
import com.almostreliable.energymeter.client.screen.widget.NumberEditBox;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.layouts.LayoutElement;
import net.minecraft.network.chat.Component;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class InputLayoutElement implements LayoutElement {

    public static final int NORMAL_TEXT_COLOR = 0xFFFF_FFFF;
    public static final int DISABLED_TEXT_COLOR = 0xFF80_8080;
    private static final int TEXT_BOX_WIDTH = 38;
    private static final int BUTTON_SIZE = 13;
    private static final int SPACING = 2;

    private final TextBoxType type;
    private final int width;
    private final int height;
    private final Supplier<String> valueSupplier;
    private final BiConsumer<TextBoxType, Integer> onValueUpdated;
    private final MarqueeStringWidget label;
    private final NumberEditBox textBox;
    private final Button confirmButton;

    public InputLayoutElement(
        TextBoxType type, int width, Font font, Component label, Supplier<String> valueSupplier,
        BiConsumer<TextBoxType, Integer> onValueUpdated
    ) {
        this.type = type;
        this.width = width;
        this.height = font.lineHeight + 4;
        this.valueSupplier = valueSupplier;
        this.onValueUpdated = onValueUpdated;
        this.label = new MarqueeStringWidget(width - BUTTON_SIZE - 1 - TEXT_BOX_WIDTH - SPACING, height, label).alignRight();
        this.textBox = new NumberEditBox(font, TEXT_BOX_WIDTH, height, valueSupplier, this::onValueEntered, this::onConfirm);
        this.confirmButton = Button.builder(Component.empty(), btn -> onConfirm()).size(BUTTON_SIZE, BUTTON_SIZE).build();
    }

    private void onValueEntered(boolean valid) {
        boolean buttonActive = valid;
        var value = textBox.getValue();
        if (value.equals(valueSupplier.get())) buttonActive = false;
        confirmButton.active = buttonActive;
    }

    private void onConfirm() {
        confirmButton.active = false;
        var value = textBox.getIntValue();
        onValueUpdated.accept(type, value);
        textBox.resetNoUpdate();
    }

    @Override
    public int getX() {
        return label.getX();
    }

    @Override
    public void setX(int x) {
        label.setX(x);
        textBox.setX(x + (width - TEXT_BOX_WIDTH - BUTTON_SIZE - 1));
        confirmButton.setX(x + width - BUTTON_SIZE);
    }

    @Override
    public int getY() {
        return label.getY();
    }

    @Override
    public void setY(int y) {
        label.setY(y);
        textBox.setY(y);
        confirmButton.setY(y);
    }

    @Override
    public int getWidth() {
        return width;
    }

    @Override
    public int getHeight() {
        return height;
    }

    @Override
    public void visitWidgets(Consumer<AbstractWidget> consumer) {
        label.visitWidgets(consumer);
        textBox.visitWidgets(consumer);
        confirmButton.visitWidgets(consumer);
    }

    public void setEnabled(boolean enabled) {
        label.setColor(enabled ? NORMAL_TEXT_COLOR : DISABLED_TEXT_COLOR);
        textBox.setEnabled(enabled);
    }

    public enum TextBoxType {

        MEASURE_INTERVAL(MeterBlockEntity::setMeasureInterval),
        ZERO_TOLERANCE(MeterBlockEntity::setZeroTolerance),
        TRANSFER_LIMIT(MeterBlockEntity::setTransferLimit);

        private final BiConsumer<MeterBlockEntity, Integer> valueUpdater;

        TextBoxType(BiConsumer<MeterBlockEntity, Integer> valueUpdater) {
            this.valueUpdater = valueUpdater;
        }

        public void updateValue(MeterBlockEntity blockEntity, int value) {
            valueUpdater.accept(blockEntity, value);
        }
    }
}
