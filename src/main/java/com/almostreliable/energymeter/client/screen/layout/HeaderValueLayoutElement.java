package com.almostreliable.energymeter.client.screen.layout;

import com.almostreliable.energymeter.client.screen.widget.SupplyingStringWidget;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.layouts.LayoutElement;
import net.minecraft.network.chat.MutableComponent;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class HeaderValueLayoutElement implements LayoutElement {

    private static final int SPACING = 2;

    private final StringWidget headerWidget;
    private final SupplyingStringWidget valueWidget;
    private final Font font;

    public HeaderValueLayoutElement(MutableComponent header, Supplier<String> valueSupplier, Font font) {
        this.headerWidget = new StringWidget(header.append(":"), font);
        this.valueWidget = new SupplyingStringWidget(valueSupplier, font);
        this.font = font;
    }

    @Override
    public void setX(int x) {
        headerWidget.setX(x);
        valueWidget.setX(x);
    }

    @Override
    public void setY(int y) {
        headerWidget.setY(y);
        valueWidget.setY(font.lineHeight + SPACING + y);
    }

    @Override
    public int getX() {
        return headerWidget.getX();
    }

    @Override
    public int getY() {
        return headerWidget.getY();
    }

    @Override
    public int getWidth() {
        return Math.max(headerWidget.getWidth(), valueWidget.getWidth());
    }

    @Override
    public int getHeight() {
        return font.lineHeight * 2 + SPACING;
    }

    @Override
    public void visitWidgets(Consumer<AbstractWidget> consumer) {
        headerWidget.visitWidgets(consumer);
        valueWidget.visitWidgets(consumer);
    }
}
