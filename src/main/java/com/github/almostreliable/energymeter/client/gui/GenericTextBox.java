package com.github.almostreliable.energymeter.client.gui;

import com.github.almostreliable.energymeter.core.Constants.UI_COLORS;
import com.github.almostreliable.energymeter.meter.MeterEntity;
import com.github.almostreliable.energymeter.network.AccuracyUpdatePacket;
import com.github.almostreliable.energymeter.network.PacketHandler;
import com.github.almostreliable.energymeter.util.GuiUtils;
import com.github.almostreliable.energymeter.util.GuiUtils.Tooltip;
import com.github.almostreliable.energymeter.util.TextUtils;
import com.github.almostreliable.energymeter.util.TypeEnums.ACCURACY;
import com.github.almostreliable.energymeter.util.TypeEnums.TEXT_BOX;
import com.github.almostreliable.energymeter.util.TypeEnums.TRANSLATE_TYPE;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.ContainerScreen;
import net.minecraft.network.chat.TextComponent;
import org.apache.commons.lang3.StringUtils;

abstract class GenericTextBox extends EditBox {

    /**
     * Holds the parent {@link ContainerScreen} the {@link Button} is rendered in.
     */
    protected final MeterScreen screen;
    private final Font font;
    private final TEXT_BOX identifier;

    GenericTextBox(
        MeterScreen screen, Font font, int pX, int pY, int width, int height, TEXT_BOX identifier
    ) {
        super(font, pX, pY, width, height, TextComponent.EMPTY);
        this.screen = screen;
        this.font = font;
        this.identifier = identifier;
        setBordered(false);
        setTextColor(UI_COLORS.WHITE);
        setFilter(text -> StringUtils.isNumeric(text) || text.isEmpty());
        setMaxLength(7);
    }

    @SuppressWarnings("SuspiciousGetterSetter")
    @Override
    public boolean isHoveredOrFocused() {
        // avoid tooltips when the text box is focused
        return isHovered;
    }

    @Override
    public void renderToolTip(PoseStack stack, int mX, int mY) {
        if (screen.getMenu().getEntity().getAccuracy() == ACCURACY.EXACT) return;
        screen.renderComponentTooltip(stack, getTooltip().resolve(), mX, mY);
    }

    protected abstract Tooltip getTooltip();

    /**
     * Overwrite this method to make it public.
     * <p>
     * This is called from the setFocus() method to actually change the focus.
     * It should be called directly to avoid validating the text box when changing focus.
     *
     * @param focused the value to set the focus to
     */
    @Override
    public void setFocused(boolean focused) {
        super.setFocused(focused);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        // submit when pressing enter
        if (keyCode == InputConstants.getKey("key.keyboard.enter").getValue()) {
            setFocus(false);
            return true;
        }
        // otherwise, just run the original functionality
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean mouseClicked(double pMouseX, double pMouseY, int pButton) {
        var clicked = super.mouseClicked(pMouseX, pMouseY, pButton);
        // reset the interval to the default value when shift clicking
        if (clicked && Screen.hasShiftDown()) {
            reset();
            // reset the threshold box as well if the interval is reset
            if (identifier == TEXT_BOX.INTERVAL) {
                screen.getThresholdBox().reset();
            }
        }
        return clicked;
    }

    @Override
    public void setFocus(boolean focused) {
        if (isFocused() && !focused) {
            // validate input on focus lose for both text boxes in case the user didn't submit
            screen.getIntervalBox().validateTextBox();
            screen.getThresholdBox().validateTextBox();
        }
        super.setFocus(focused);
    }

    @Override
    public void renderButton(PoseStack stack, int mX, int mY, float partial) {
        if (screen.getMenu().getEntity().getAccuracy() == ACCURACY.EXACT) return;
        var label = TextUtils.translateAsString(TRANSLATE_TYPE.LABEL, identifier.toString().toLowerCase()) + ":";
        var labelWidth = font.width(label);
        // expand the tooltip range to the text box
        isHovered = mX >= x - 4 - labelWidth && mY >= y - 3 && mX < x + width + 3 && mY < y + height + 3;
        // render small identifier label in front of the box
        GuiUtils.renderText(stack, x - 4 - labelWidth, y, 1, label, UI_COLORS.WHITE);
        // render the text box with a small gap to the border
        fill(stack, x - 3, y - 3, x + width + 3, y + height + 3, -65_434);
        fill(stack, x - 2, y - 2, x + width + 2, y + height + 2, -15_263_977);
        super.renderButton(stack, mX, mY, partial);
    }

    /**
     * Resets the text field to the refresh rate of the meter and syncs it.
     */
    void reset() {
        changeTextBoxValue(MeterEntity.REFRESH_RATE, true);
    }

    /**
     * Checks if the current value of the text box is valid and syncs it if it changed.
     * <p>
     * Will replace the text with the previous value if invalid.
     */
    void validateTextBox() {
        var oldValue = getOldValue();
        int value;
        try {
            value = Integer.parseInt(getValue());
        } catch (NumberFormatException e) {
            changeTextBoxValue(oldValue, false);
            return;
        }

        if (value != oldValue) changeTextBoxValue(value, true);
    }

    protected abstract int getOldValue();

    /**
     * Changes the text of the text box to the specified integer.
     * Automatically replaces the specified value with the minimum amount if it's lower.
     * <p>
     * When a true boolean is passed, the new value will be synced to the server.
     *
     * @param value the value to place in the text box
     * @param sync  whether the value should be synced to the server
     */
    protected void changeTextBoxValue(int value, boolean sync) {
        setValue(String.valueOf(Math.max(value, MeterEntity.REFRESH_RATE)));
        if (sync) {
            PacketHandler.CHANNEL.sendToServer(new AccuracyUpdatePacket(identifier,
                Math.max(value, MeterEntity.REFRESH_RATE)
            ));
        }
    }
}
