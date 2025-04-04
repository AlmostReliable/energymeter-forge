package com.almostreliable.energymeter.client.screen.layout;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.layouts.LayoutElement;
import net.minecraft.network.chat.Component;

import java.util.function.Consumer;

public class InputLayoutElement implements LayoutElement {

    private static final int ELEMENT_WIDTH = 115;
    private static final int TEXT_BOX_WIDTH = 50;
    private static final int SPACING = 5;

    private final int height;
    private final StringWidget label;
    private final EditBox textBox;

    public InputLayoutElement(Component label, Font font) {
        this.height = font.lineHeight + 4;
        this.label = new StringWidget(ELEMENT_WIDTH - SPACING - TEXT_BOX_WIDTH, height, label, font).alignRight();
        this.textBox = new EditBox(font, TEXT_BOX_WIDTH, height, Component.empty());
    }

    public InputLayoutElement set(String value) {
        textBox.setValue(value);
        return this;
    }

    @Override
    public int getX() {
        return label.getX();
    }

    @Override
    public void setX(int x) {
        label.setX(x);
        textBox.setX(x + (ELEMENT_WIDTH - TEXT_BOX_WIDTH));
    }

    @Override
    public int getY() {
        return label.getY();
    }

    @Override
    public void setY(int y) {
        label.setY(y);
        textBox.setY(y);
    }

    @Override
    public int getWidth() {
        return ELEMENT_WIDTH;
    }

    @Override
    public int getHeight() {
        return height;
    }

    @Override
    public void visitWidgets(Consumer<AbstractWidget> consumer) {
        label.visitWidgets(consumer);
        textBox.visitWidgets(consumer);
    }
}
