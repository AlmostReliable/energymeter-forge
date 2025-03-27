package com.almostreliable.energymeter.client.screen.widget;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.network.chat.Component;

import java.util.function.Supplier;

public class SupplyingStringWidget extends StringWidget {

    private final Supplier<Component> component;

    public SupplyingStringWidget(Supplier<Component> component, Font font) {
        super(component.get(), font);
        this.component = component;
        alignLeft();
    }

    @Override
    public Component getMessage() {
        return component.get();
    }
}
