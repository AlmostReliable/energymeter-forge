package com.almostreliable.energymeter.client.screen.widget;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;

abstract class PositionlessWidget extends AbstractWidget {

    final Font font;

    PositionlessWidget(int width, int height) {
        super(0, 0, width, height, Component.empty());
        this.font = Minecraft.getInstance().font;
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
        defaultButtonNarrationText(narrationElementOutput);
    }
}
