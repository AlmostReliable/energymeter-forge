package com.almostreliable.energymeter.client.screen.widget;

import com.almostreliable.energymeter.client.screen.widget.base.ClickedOutsideListener;
import com.almostreliable.energymeter.util.MathExpressionParser;
import com.almostreliable.energymeter.util.TooltipBuilder;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;

import com.google.common.base.Preconditions;
import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import org.lwjgl.glfw.GLFW;

import org.jetbrains.annotations.Nullable;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.function.Supplier;
import java.util.regex.Pattern;

/**
 * Inspired by Applied Energistics 2's NumberEntryWidget.
 * <p>
 * Uses the {@link MathExpressionParser} to calculate a math expression input
 * and displays it as a {@link Tooltip}.
 */
public class NumberEditBox extends EditBox implements ClickedOutsideListener {

    private static final int NORMAL_TEXT_COLOR = 0xFFFF_FFFF;
    private static final int ERROR_TEXT_COLOR = 0xFFFF_0000;

    private static final Pattern MATH_EXPRESSION_PATTERN = Pattern.compile("\\d\\s*[+\\-*/^]\\s*\\d");
    private static final String BINARY_OPERATORS = "+*/^";
    private static final String OPERATORS = BINARY_OPERATORS + "-";
    private static final char NONE_CHAR = '\0';

    // TODO:
    //  - cache initial value
    //  - automatically unfocuses so don't show value from value supplier if focused (edited)
    //  - don't show value from value supplier if it has been focused and the value changed without confirmation
    //  - show indicator when the value has been edited (maybe by marking activating the submit button)

    private final Supplier<String> valueSupplier;
    private final BooleanConsumer onValueEntered;
    private final Runnable onConfirm;

    private boolean newValueEntered;
    @Nullable
    private BigDecimal parsedValue;
    @Nullable
    private String tooltip;

    public NumberEditBox(
        Font font, int width, int height, Supplier<String> valueSupplier, BooleanConsumer onValueEntered, Runnable onConfirm) {
        super(font, width, height, Component.empty());
        this.valueSupplier = valueSupplier;
        this.onValueEntered = onValueEntered;
        this.onConfirm = onConfirm;

        setMaxLength(128);
        setTextColor(NORMAL_TEXT_COLOR);
        setTextShadow(false);
        setResponder(this::onValueChanged);
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        updateValueFromServer();
        super.renderWidget(guiGraphics, mouseX, mouseY, partialTick);
        if (tooltip != null) {
            var mc = Minecraft.getInstance();
            var tooltipSequence = TooltipBuilder.create().literal(tooltip).build().toCharSequence(mc);
            var tooltipWidth = mc.font.width(tooltipSequence.getFirst()) + 16;
            guiGraphics.renderTooltip(mc.font, tooltipSequence, getX() + width - tooltipWidth, getY());
        }
    }

    private void updateValueFromServer() {
        if (newValueEntered) return;
        var text = valueSupplier.get();
        if (text.equals(getValue())) return;
        setValue(text);
    }

    @Override
    protected boolean isValidClickButton(int button) {
        return button == GLFW.GLFW_MOUSE_BUTTON_LEFT || button == GLFW.GLFW_MOUSE_BUTTON_RIGHT;
    }

    @Override
    public void onClick(double mouseX, double mouseY, int button) {
        if (button == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
            setValue("");
            newValueEntered = true;
            return;
        }
        super.onClick(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (canConsumeInput() && (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER)) {
            if (parsedValue != null) {
                Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                reset();
                setFocused(false);
                onConfirm.run();
            }
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void onClickedOutside() {
        setFocused(false);
    }

    @Override
    public void setFocused(boolean focused) {
        super.setFocused(focused);
        if (!focused && getValue().isEmpty()) {
            reset();
            return;
        }
        tooltip = null;
    }

    private void onValueChanged(String text) {
        tooltip = null;

        validateAndUpdate();

        if (parsedValue == null) {
            setTextColor(ERROR_TEXT_COLOR);
        } else {
            setTextColor(NORMAL_TEXT_COLOR);
        }
        onValueEntered.accept(parsedValue != null);
    }

    @Override
    public void insertText(String text) {
        StringBuilder filtered = new StringBuilder();
        for (char c : text.toCharArray()) {
            if (canInsertCharacter(c)) {
                filtered.append(c);
                newValueEntered = true;
            }
        }
        super.insertText(filtered.toString());
    }

    @SuppressWarnings("RedundantIfStatement")
    private boolean canInsertCharacter(char c) {
        if (!isValidCharacter(c)) return false;
        if (Character.isWhitespace(c)) return true;

        char prev = previousNonWhitespace();
        if (prev == NONE_CHAR && isBinaryOperator(c)) return false;

        if (isOperator(prev) && isOperator(c)) {
            return c == '-' && prev != ')';
        }

        if (c == ')' && isOperator(prev)) return false;
        if (c == ')' && !hasOpeningParenthesis()) return false;
        if (prev == '(' && isBinaryOperator(c)) return false;

        if (c == '(' && prev != NONE_CHAR && !isOperator(prev)) {
            return false;
        }

        if (c == '.' && hasDecimalInCurrentNumber()) return false;

        return true;
    }

    private boolean isValidCharacter(char c) {
        return Character.isDigit(c)
            || OPERATORS.indexOf(c) >= 0
            || c == '.' || c == '(' || c == ')'
            || Character.isWhitespace(c);
    }

    private char previousNonWhitespace() {
        String value = getValue();
        int cursor = getCursorPosition();

        for (int i = cursor - 1; i >= 0; i--) {
            char c = value.charAt(i);
            if (!Character.isWhitespace(c)) {
                return c;
            }
        }
        return NONE_CHAR;
    }

    private boolean hasOpeningParenthesis() {
        String value = getValue();
        int cursor = getCursorPosition();

        int openCount = 0;
        int closeCount = 0;

        for (int i = cursor - 1; i >= 0; i--) {
            char c = value.charAt(i);
            if (c == '(') {
                openCount++;
            } else if (c == ')') {
                closeCount++;
            }
        }

        return openCount > closeCount;
    }

    private boolean hasDecimalInCurrentNumber() {
        String value = getValue();
        int cursor = getCursorPosition();

        for (int i = cursor - 1; i >= 0; i--) {
            char c = value.charAt(i);
            if (c == '.') return true;
            if (!Character.isDigit(c)) break;
        }
        return false;
    }

    private boolean isOperator(char c) {
        return OPERATORS.indexOf(c) >= 0;
    }

    private boolean isBinaryOperator(char c) {
        return BINARY_OPERATORS.indexOf(c) >= 0;
    }

    private void validateAndUpdate() {
        var textValue = getValue();
        var isMathExpression = MATH_EXPRESSION_PATTERN.matcher(textValue.trim()).find();

        parsedValue = MathExpressionParser.parse(textValue).orElse(null);
        if (parsedValue == null) return;

        if (parsedValue.scale() > 0) {
            parsedValue = parsedValue.setScale(0, RoundingMode.HALF_UP);
            if (isMathExpression) {
                tooltip = "≈ " + parsedValue.toPlainString();
            }
        } else if (isMathExpression) {
            tooltip = "= " + parsedValue.toPlainString();
        }
    }

    public void resetNoUpdate() {
        newValueEntered = false;
        parsedValue = null;
        tooltip = null;
    }

    public void reset() {
        resetNoUpdate();
        updateValueFromServer();
    }

    public int getIntValue() {
        Preconditions.checkNotNull(parsedValue, "value needs to be parsed first");
        Preconditions.checkArgument(parsedValue.scale() <= 0, "value needs to be rounded to an integer");
        return parsedValue.intValueExact();
    }
}
