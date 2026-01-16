package com.almostreliable.energymeter.client.screen.widget;

import com.almostreliable.energymeter.util.MathExpressionParser;
import com.almostreliable.energymeter.util.TooltipBuilder;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;

import org.lwjgl.glfw.GLFW;

import org.jetbrains.annotations.Nullable;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.regex.Pattern;

/**
 * Inspired by Applied Energistics 2's NumberEntryWidget.
 * <p>
 * Uses the {@link MathExpressionParser} to calculate a math expression input
 * and displays it as a {@link Tooltip}.
 */
public class NumberEditBox extends EditBox {

    private static final int NORMAL_TEXT_COLOR = 0xFFFF_FFFF;
    private static final int ERROR_TEXT_COLOR = 0xFFFF_0000;

    private static final Pattern MATH_EXPRESSION_PATTERN = Pattern.compile("[+\\-*/^()]");
    private static final String BINARY_OPERATORS = "+*/^";
    private static final String OPERATORS = BINARY_OPERATORS + "-";
    private static final char NONE_CHAR = '\0';

    private boolean isValid = true;
    @Nullable
    private BigDecimal lastParsedValue;
    @Nullable
    private Runnable onConfirm;

    public NumberEditBox(Font font, int width, int height) {
        super(font, width, height, Component.empty());
        setMaxLength(128);
        setTextColor(NORMAL_TEXT_COLOR);
        setFGColor(0xFF00_FFA2);
        setTextShadow(false);
        setResponder(this::onTextChanged);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (canConsumeInput() && (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER)) {
            if (onConfirm != null && getLongValue().isPresent()) {
                onConfirm.run();
            }
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void insertText(String text) {
        StringBuilder filtered = new StringBuilder();
        for (char c : text.toCharArray()) {
            if (canInsertCharacter(c)) {
                filtered.append(c);
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

    private void onTextChanged(String text) {
        validate();
    }

    private void validate() {
        Optional<BigDecimal> parsed = MathExpressionParser.parse(getValue());
        lastParsedValue = parsed.orElse(null);

        boolean wasValid = isValid;
        isValid = lastParsedValue != null;

        if (isValid != wasValid) {
            setTextColor(isValid ? NORMAL_TEXT_COLOR : ERROR_TEXT_COLOR);
        }

        if (isValid && isMathExpression(getValue())) {
            // noinspection OptionalGetWithoutIsPresent
            setTooltip(TooltipBuilder.create().literal("= " + parsed.get().stripTrailingZeros().toPlainString()).build());
        } else {
            setTooltip(null);
        }
    }

    private boolean isMathExpression(String text) {
        return MATH_EXPRESSION_PATTERN.matcher(text.trim()).find();
    }

    public OptionalLong getLongValue() {
        if (!isValid || lastParsedValue == null) return OptionalLong.empty();
        if (lastParsedValue.scale() > 0) return OptionalLong.empty();
        return OptionalLong.of(lastParsedValue.longValueExact());
    }

    public void setOnConfirm(Runnable callback) {
        this.onConfirm = callback;
    }
}
