package com.github.almostreliable.energymeter.client.gui;

import com.github.almostreliable.energymeter.core.Constants.UI_COLORS;
import com.github.almostreliable.energymeter.meter.MeterEntity;
import com.github.almostreliable.energymeter.util.GuiUtils.Tooltip;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.ContainerScreen;
import net.minecraft.network.chat.TextComponent;
import org.apache.commons.lang3.StringUtils;

public class TextBox extends EditBox {

    /**
     * Holds the parent {@link ContainerScreen} the {@link Button} is rendered in.
     */
    private final MeterScreen screen;

    TextBox(MeterScreen screen, Font font, int pX, int pY, int width, int height) {
        super(font, pX, pY, width, height, TextComponent.EMPTY);
        this.screen = screen;
        setBordered(false);
        setTextColor(UI_COLORS.WHITE);
        setFilter(text -> StringUtils.isNumeric(text) || text.isEmpty());
        setMaxLength(7);
        setValue(String.valueOf(screen.getMenu().getEntity().getInterval()));
    }

    @Override
    public void render(PoseStack stack, int mX, int mY, float partial) {
        super.render(stack, mX, mY, partial);
        if (isHovered) renderToolTip(stack, mX, mY);
    }

    @Override
    public void renderToolTip(PoseStack stack, int mX, int mY) {
        var tooltip = Tooltip.builder()
            // header
            .addHeader("interval")
            .addBlankLine()
            // description
            .addDescription("interval_desc_1")
            .addDescription("interval_desc_2")
            .addBlankLine()
            // action
            .addClickAction("action_5")
            .addShiftClickAction("action_2")
            .addCustomAction("key.keyboard.enter", "action_6");

        screen.renderComponentTooltip(stack, tooltip.resolve(), mX, mY);
    }

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
        boolean clicked = super.mouseClicked(pMouseX, pMouseY, pButton);
        // reset the interval to the default value when shift clicking
        if (clicked && Screen.hasShiftDown()) screen.changeTextBoxValue(MeterEntity.REFRESH_RATE, true);
        return clicked;
    }

    @Override
    public void setFocus(boolean focused) {
        if (isFocused() && !focused) {
            // validate input on focus lose
            screen.validateTextBox();
        }
        super.setFocus(focused);
    }

    @Override
    public void renderButton(PoseStack stack, int mX, int mY, float partial) {
        fill(stack, x - 3, y - 3, x + width + 3, y + height + 3, -65_434);
        fill(stack, x - 2, y - 2, x + width + 2, y + height + 2, -15_263_977);
        super.renderButton(stack, mX, mY, partial);
    }
}
