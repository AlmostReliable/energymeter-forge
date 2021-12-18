package com.github.almostreliable.energymeter.client.gui;

import com.github.almostreliable.energymeter.meter.MeterEntity;
import com.github.almostreliable.energymeter.util.GuiUtils.Tooltip;
import com.github.almostreliable.energymeter.util.TypeEnums;
import com.github.almostreliable.energymeter.util.TypeEnums.TEXT_BOX;
import net.minecraft.client.gui.Font;

class ThresholdBox extends GenericTextBox {

    private static final Tooltip TOOLTIP = setupTooltip();

    ThresholdBox(MeterScreen screen, Font font, int pX, int pY, int width, int height) {
        super(screen, font, pX, pY, width, height, TEXT_BOX.THRESHOLD);
        setValue(String.valueOf(screen.getMenu().getEntity().getThreshold()));
    }

    private static Tooltip setupTooltip() {
        return Tooltip.builder()
            // header
            .addHeader("threshold")
            .addBlankLine()
            // description
            .addDescription("threshold_desc_1")
            .addDescription("threshold_desc_2")
            .addBlankLine()
            // action
            .addClickAction("action_5")
            .addShiftClickAction("action_2")
            .addCustomAction("key.keyboard.enter", "action_6");
    }

    @Override
    protected Tooltip getTooltip() {
        return TOOLTIP;
    }

    @Override
    protected int getOldValue() {
        return screen.getMenu().getEntity().getThreshold();
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
}
