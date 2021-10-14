package dev.rlnt.energymeter.client.gui;

import dev.rlnt.energymeter.meter.MeterTile;
import dev.rlnt.energymeter.util.Tooltip;
import javax.annotation.Nullable;

public class ResetButton extends LabelButton {

    private static final int TEXTURE_WIDTH = 58;
    private static final int TEXTURE_HEIGHT = 19;
    private final Tooltip tooltip;

    ResetButton(MeterScreen screen, int pX, int pY) {
        super(screen, pX, pY, TEXTURE_WIDTH, TEXTURE_HEIGHT, "RESET");
        tooltip = setupTooltip();
    }

    @Override
    protected void clickHandler() {
        screen.getTextBox().setFocused(false);
        screen.changeTextBoxValue(MeterTile.REFRESH_RATE, true);
    }

    @Override
    protected String getTexture() {
        return "reset";
    }

    @Override
    protected int getTextureWidth() {
        return TEXTURE_WIDTH;
    }

    @Override
    protected int getTextureHeight() {
        return TEXTURE_HEIGHT;
    }

    @Override
    protected Tooltip setupTooltip() {
        return Tooltip
            .builder()
            // header
            .addHeader("reset")
            .addBlankLine()
            // description
            .addDescription("reset_desc")
            .addBlankLine()
            // action
            .addClickAction("action_4");
    }

    @Nullable
    @Override
    protected Tooltip getTooltip() {
        return tooltip;
    }
}
