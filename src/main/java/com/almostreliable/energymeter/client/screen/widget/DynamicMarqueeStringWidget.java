package com.almostreliable.energymeter.client.screen.widget;

import net.minecraft.network.chat.Component;

import java.util.function.Supplier;

public class DynamicMarqueeStringWidget extends MarqueeStringWidget {

    private final Supplier<Component> textSupplier;

    @SuppressWarnings("AssignmentToSuperclassField")
    public DynamicMarqueeStringWidget(int width, Supplier<Component> textSupplier) {
        super(width, 0, Component.empty());
        this.textSupplier = textSupplier;
        this.height = font.lineHeight - 2;
    }

    @Override
    public Component getMessage() {
        return textSupplier.get();
    }
}
