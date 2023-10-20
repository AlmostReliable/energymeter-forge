package com.github.almostreliable.energymeter.client.gui;

import com.github.almostreliable.energymeter.util.GuiUtils.TooltipBuilder;
import com.github.almostreliable.energymeter.util.TypeEnums.TEXT_BOX;
import net.minecraft.client.gui.Font;

class IntervalBox extends GenericTextBox {

    private static final TooltipBuilder TOOLTIP = setupTooltip();

    IntervalBox(MeterScreen screen, Font font, int pX, int pY, int width, int height) {
        super(screen, font, pX, pY, width, height, TEXT_BOX.INTERVAL);
        setValue(String.valueOf(screen.getMenu().getEntity().getInterval()));
    }

    private static TooltipBuilder setupTooltip() {
        return TooltipBuilder
            .builder()
            .addHeader("interval")
            .addBlankLine()
            .addDescription("interval_desc_1")
            .addDescription("interval_desc_2")
            .addBlankLine()
            .addClickAction("action_5")
            .addShiftClickAction("action_2")
            .addCustomAction("key.keyboard.enter", "action_6");
    }

    @Override
    protected TooltipBuilder getTooltipBuilder() {
        return TOOLTIP;
    }

    @Override
    protected int getOldValue() {
        return screen.getMenu().getEntity().getInterval();
    }
}
