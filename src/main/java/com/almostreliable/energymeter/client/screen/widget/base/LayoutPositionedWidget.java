package com.almostreliable.energymeter.client.screen.widget.base;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;

public abstract class LayoutPositionedWidget extends AbstractWidget {

    protected final Font font;

    protected LayoutPositionedWidget(int width, int height, Component message) {
        super(0, 0, width, height, message);
        this.font = Minecraft.getInstance().font;
    }

    protected LayoutPositionedWidget(int width, int height) {
        this(width, height, Component.empty());
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
        defaultButtonNarrationText(narrationElementOutput);
    }
}
