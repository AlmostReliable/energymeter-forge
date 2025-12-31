package com.almostreliable.energymeter.client.screen.widget;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;

import java.util.function.Supplier;

public class NumberEditBox extends EditBox {

    private final Supplier<Long> valueSupplier;

    public NumberEditBox(Font font, int width, int height, Supplier<Long> valueSupplier) {
        super(font, width, height, Component.empty());
        this.valueSupplier = valueSupplier;
        setMaxLength(128);
        setTextColor(0xFFFF_FFFF);
        setFGColor(0xFF00_FFA2);
        setTextShadow(false);
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        refreshValue();
        super.renderWidget(guiGraphics, mouseX, mouseY, partialTick);
    }

    private void refreshValue() {
        var v = valueSupplier.get();
    }
}
