package com.github.almostreliable.energymeter.client.gui;

import com.github.almostreliable.energymeter.meter.MeterEntity;
import com.github.almostreliable.energymeter.util.GuiUtils.TooltipBuilder;
import com.github.almostreliable.energymeter.util.TypeEnums.TEXT_BOX;
import net.minecraft.client.gui.Font;

class ThresholdBox extends GenericTextBox {

    private static final TooltipBuilder TOOLTIP = setupTooltip();

    ThresholdBox(MeterScreen screen, Font font, int pX, int pY, int width, int height) {
        super(screen, font, pX, pY, width, height, TEXT_BOX.THRESHOLD);
        setValue(String.valueOf(screen.getMenu().getEntity().getThreshold()));
    }

    private static TooltipBuilder setupTooltip() {
        return TooltipBuilder
            .builder()
            .addHeader("threshold")
            .addBlankLine()
            .addDescription("threshold_desc_1")
            .addDescription("threshold_desc_2")
            .addBlankLine()
            .addClickAction("action_5")
            .addShiftClickAction("action_2")
            .addCustomAction("key.keyboard.enter", "action_6");
    }

    @Override
    protected void changeTextBoxValue(int value, boolean sync) {
        int newValue;
        try {
            newValue = Math.min(value, Integer.parseInt(screen.getIntervalBox().getValue()));
        } catch (NumberFormatException e) {
            newValue = MeterEntity.REFRESH_RATE;
        }
        super.changeTextBoxValue(newValue, sync);
    }

    @Override
    protected TooltipBuilder getTooltipBuilder() {
        return TOOLTIP;
    }

    @Override
    protected int getOldValue() {
        return screen.getMenu().getEntity().getThreshold();
    }
}
