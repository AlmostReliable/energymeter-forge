package com.almostreliable.energymeter.client.screen.widget;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.network.chat.Component;

import java.util.function.Supplier;

public class SupplyingStringWidget extends StringWidget {

    private static final String LONGEST_STRING = "################";
    private final Supplier<String> stringSupplier;

    public SupplyingStringWidget(Supplier<String> stringSupplier, Font font) {
        super(Component.literal(LONGEST_STRING), font);
        this.stringSupplier = stringSupplier;
        alignLeft();
    }

    @Override
    public Component getMessage() {
        return Component.literal(stringSupplier.get());
    }
}
