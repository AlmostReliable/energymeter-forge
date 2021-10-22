package dev.rlnt.energymeter.client.gui;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.rlnt.energymeter.util.Tooltip;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
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
        Tooltip tooltip = Tooltip
            .builder()
            // header
            .addHeader("interval")
            .addBlankLine()
            // description
            .addDescription("interval_desc_1")
            .addDescription("interval_desc_2")
            .addBlankLine()
            // action
            .addClickAction("action_5")
            .addCustomAction("key.keyboard.enter", "action_6");

        screen.renderComponentTooltip(stack, tooltip.get(), mX, mY);
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
    public void setFocus(boolean focused) {
        if (isFocused() && !focused) {
            // validate input on focus lose
            screen.validateTextBox();
        }
        super.setFocus(focused);
    }
}
