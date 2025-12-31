package com.almostreliable.energymeter.client.screen.widget;

import com.almostreliable.energymeter.client.screen.widget.base.LayoutPositionedWidget;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

import java.util.function.Supplier;

public class DynamicTextWidget extends LayoutPositionedWidget {

    private final Supplier<Component> textSupplier;

    @SuppressWarnings("AssignmentToSuperclassField")
    public DynamicTextWidget(int width, Supplier<Component> textSupplier) {
        super(width, 0, Component.empty());
        this.height = font.lineHeight - 2;
        this.textSupplier = textSupplier;
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        guiGraphics.drawString(font, textSupplier.get(), getX(), getY(), 0xFFFF_FFFF);
    }
}
