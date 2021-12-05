package dev.rlnt.energymeter.client.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import dev.rlnt.energymeter.core.Constants.UI_COLORS;
import dev.rlnt.energymeter.util.GuiUtils.Tooltip;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.util.InputMappings;
import net.minecraft.util.text.StringTextComponent;
import org.apache.commons.lang3.StringUtils;

class TextBox extends TextFieldWidget {

    /**
     * Holds the parent {@link ContainerScreen} the {@link Button} is rendered in.
     */
    private final MeterScreen screen;

    TextBox(MeterScreen screen, FontRenderer font, int pX, int pY, int width, int height) {
        super(font, pX, pY, width, height, StringTextComponent.EMPTY);
        this.screen = screen;
        setBordered(false);
        setTextColor(UI_COLORS.WHITE);
        setFilter(text -> StringUtils.isNumeric(text) || text.isEmpty());
        setMaxLength(7);
        setValue(String.valueOf(screen.getMenu().getTile().getInterval()));
    }

    @Override
    public void render(MatrixStack matrix, int mX, int mY, float partial) {
        super.render(matrix, mX, mY, partial);
        if (isHovered) renderToolTip(matrix, mX, mY);
    }

    @Override
    public void renderToolTip(MatrixStack matrix, int mX, int mY) {
        Tooltip tooltip = Tooltip.builder()
            // header
            .addHeader("interval").addBlankLine()
            // description
            .addDescription("interval_desc_1").addDescription("interval_desc_2").addBlankLine()
            // action
            .addClickAction("action_5").addCustomAction("key.keyboard.enter", "action_6");

        screen.renderComponentTooltip(matrix, tooltip.resolve(), mX, mY);
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
        if (keyCode == InputMappings.getKey("key.keyboard.enter").getValue()) {
            setFocus(false);
            return true;
        }
        // otherwise, just run the original functionality
        return super.keyPressed(keyCode, scanCode, modifiers);
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
    public void renderButton(MatrixStack matrix, int mX, int mY, float partial) {
        fill(matrix, x - 1, y - 1, x + width + 1, y + height + 1, -65_434);
        fill(matrix, x, y, x + width, y + height, -15_263_977);
        super.renderButton(matrix, mX, mY, partial);
    }
}
