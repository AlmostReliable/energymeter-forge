package com.github.almostreliable.energymeter.util;

import com.github.almostreliable.energymeter.util.TypeEnums.TRANSLATE_TYPE;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.util.InputMappings;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;

import java.util.ArrayList;
import java.util.List;

public enum GuiUtils {
    ;

    /**
     * Draws a given text at the given position with the given color.
     * <p>
     * This method handles the translation and scaling of the text in order
     * to maintain the same position after the scaling.
     *
     * @param matrix the matrix stack for the drawing
     * @param x      the x position
     * @param y      the y position
     * @param scale  the scale of the text
     * @param text   the text to draw
     * @param color  the color of the text as decimal
     */
    public static void renderText(MatrixStack matrix, int x, int y, float scale, String text, int color) {
        matrix.pushPose();
        matrix.translate(x, y, 0);
        matrix.scale(scale, scale, 1);
        Minecraft.getInstance().font.draw(matrix, text, 0, 0, color);
        matrix.popPose();
    }

    @SuppressWarnings("UnusedReturnValue")
    public static final class Tooltip {

        private final List<ITextComponent> value = new ArrayList<>();

        private Tooltip() {}

        public static Tooltip builder() {
            return new Tooltip();
        }

        public List<ITextComponent> resolve() {
            return value;
        }

        /**
         * Adds a blank line to the tooltip.
         *
         * @return the instance of the tooltip
         */
        public Tooltip addBlankLine() {
            return addComponent(new StringTextComponent(" "));
        }

        /**
         * Adds a generic component to the tooltip.
         *
         * @param component the component to add
         * @return the instance of the tooltip
         */
        public Tooltip addComponent(ITextComponent component) {
            value.add(component);
            return this;
        }

        /**
         * Adds a header component to the tooltip.
         *
         * @param key the key of the translation
         * @return the instance of the tooltip
         */
        public Tooltip addHeader(String key) {
            return addComponent(TextUtils.translate(TRANSLATE_TYPE.TOOLTIP, key, TextFormatting.GOLD));
        }

        /**
         * Adds a description component to the tooltip.
         *
         * @param key the key of the translation
         * @return the instance of the tooltip
         */
        public Tooltip addDescription(String key) {
            return addComponent(TextUtils.translate(TRANSLATE_TYPE.TOOLTIP, key, TextFormatting.WHITE));
        }

        /**
         * Adds a custom action component to the tooltip.
         *
         * @param input the key of the {@link InputMappings}
         * @param key   the key of the translation
         * @return the instance of the tooltip
         */
        public Tooltip addCustomAction(String input, String key) {
            return addComponent(TextUtils
                .colorize("> ", TextFormatting.GRAY)
                .append(TextUtils.colorize(InputMappings.getKey(input).getDisplayName().getString(),
                    TextFormatting.AQUA
                ))
                .append(" ")
                .append(TextUtils.translate(TRANSLATE_TYPE.TOOLTIP, key, TextFormatting.GRAY)));
        }

        /**
         * Adds a click action component to the tooltip.
         *
         * @param key the key of the translation
         * @return the instance of the tooltip
         */
        public Tooltip addClickAction(String key) {
            return addComponent(TextUtils
                .colorize("> ", TextFormatting.GRAY)
                .append(TextUtils.translate(TRANSLATE_TYPE.TOOLTIP, "action_click", TextFormatting.AQUA))
                .append(" ")
                .append(TextUtils.translate(TRANSLATE_TYPE.TOOLTIP, key, TextFormatting.GRAY)));
        }

        /**
         * Adds a hold action component to the tooltip.
         *
         * @param input the key of the {@link InputMappings}
         * @param key   the key of the translation
         * @return the instance of the tooltip
         */
        public Tooltip addHoldAction(String input, String key) {
            return addComponent(TextUtils
                .colorize("> ", TextFormatting.GRAY)
                .append(TextUtils.translate(TRANSLATE_TYPE.TOOLTIP, "action_hold", TextFormatting.GRAY))
                .append(" ")
                .append(TextUtils.colorize(InputMappings.getKey(input).getDisplayName().getString(),
                    TextFormatting.AQUA
                ))
                .append(" ")
                .append(TextUtils.translate(TRANSLATE_TYPE.TOOLTIP, key, TextFormatting.GRAY)));
        }

        /**
         * Adds a shift click action component to the tooltip.
         *
         * @param key the key of the translation
         * @return the instance of the tooltip
         */
        public Tooltip addShiftClickAction(String key) {
            return addComponent(TextUtils
                .colorize("> ", TextFormatting.GRAY)
                .append(TextUtils.colorize(String.format("%s + %s",
                    InputMappings.getKey("key.keyboard.left.shift").getDisplayName().getString(),
                    TextUtils.translateAsString(TRANSLATE_TYPE.TOOLTIP, "action_click")
                ), TextFormatting.AQUA))
                .append(" ")
                .append(TextUtils.translate(TRANSLATE_TYPE.TOOLTIP, key, TextFormatting.GRAY)));
        }
    }
}
