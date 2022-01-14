package com.github.almostreliable.energymeter.client.gui;

import com.github.almostreliable.energymeter.meter.MeterTile;
import com.github.almostreliable.energymeter.util.GuiUtils.Tooltip;
import com.github.almostreliable.energymeter.util.TypeEnums.TEXT_BOX;
import net.minecraft.client.gui.FontRenderer;

class ThresholdBox extends GenericTextBox {

    private static final Tooltip TOOLTIP = setupTooltip();

    ThresholdBox(MeterScreen screen, FontRenderer font, int pX, int pY, int width, int height) {
        super(screen, font, pX, pY, width, height, TEXT_BOX.THRESHOLD);
        setValue(String.valueOf(screen.getMenu().getTile().getThreshold()));
    }

    private static Tooltip setupTooltip() {
        return Tooltip
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
            newValue = MeterTile.REFRESH_RATE;
        }
        super.changeTextBoxValue(newValue, sync);
    }

    @Override
    protected Tooltip getTooltip() {
        return TOOLTIP;
    }

    @Override
    protected int getOldValue() {
        return screen.getMenu().getTile().getThreshold();
    }
}
