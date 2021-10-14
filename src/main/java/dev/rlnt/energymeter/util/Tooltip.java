package dev.rlnt.energymeter.util;

import dev.rlnt.energymeter.util.TypeEnums.TRANSLATE_TYPE;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.util.InputMappings;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;

public class Tooltip {

    private final List<ITextComponent> value = new ArrayList<>();

    private Tooltip() {}

    public static Tooltip builder() {
        return new Tooltip();
    }

    public List<ITextComponent> get() {
        return value;
    }

    /**
     * Adds a generic component to the tooltip.
     *
     * @param component the component to add
     * @return the instance of the tooltip
     */
    public Tooltip add(ITextComponent component) {
        value.add(component);
        return this;
    }

    /**
     * Adds a blank line to the tooltip.
     *
     * @return the instance of the tooltip
     */
    public Tooltip addBlankLine() {
        return add(new StringTextComponent(" "));
    }

    /**
     * Adds a header component to the tooltip.
     *
     * @param key the key of the translation
     * @return the instance of the tooltip
     */
    public Tooltip addHeader(String key) {
        return add(TextUtils.translate(TRANSLATE_TYPE.TOOLTIP, key, TextFormatting.GOLD));
    }

    /**
     * Adds a description component to the tooltip.
     *
     * @param key the key of the translation
     * @return the instance of the tooltip
     */
    public Tooltip addDescription(String key) {
        return add(TextUtils.translate(TRANSLATE_TYPE.TOOLTIP, key, TextFormatting.WHITE));
    }

    /**
     * Adds a custom action component to the tooltip.
     *
     * @param input the key of the input mapping
     * @param key   the key of the translation
     * @return the instance of the tooltip
     */
    public Tooltip addCustomAction(String input, String key) {
        return add(
            TextUtils
                .colorize("> ", TextFormatting.GRAY)
                .append(
                    TextUtils.colorize(InputMappings.getKey(input).getDisplayName().getString(), TextFormatting.AQUA)
                )
                .append(" ")
                .append(TextUtils.translate(TRANSLATE_TYPE.TOOLTIP, key, TextFormatting.GRAY))
        );
    }

    /**
     * Adds a click action component to the tooltip.
     *
     * @param key the key of the translation
     * @return the instance of the tooltip
     */
    public Tooltip addClickAction(String key) {
        return add(
            TextUtils
                .colorize("> ", TextFormatting.GRAY)
                .append(TextUtils.translate(TRANSLATE_TYPE.TOOLTIP, "action_click", TextFormatting.AQUA))
                .append(" ")
                .append(TextUtils.translate(TRANSLATE_TYPE.TOOLTIP, key, TextFormatting.GRAY))
        );
    }

    /**
     * Adds a shift click action component to the tooltip.
     *
     * @param key the key of the translation
     * @return the instance of the tooltip
     */
    public Tooltip addShiftClickAction(String key) {
        return add(
            TextUtils
                .colorize("> ", TextFormatting.GRAY)
                .append(
                    TextUtils.colorize(
                        String.format(
                            "%s + %s",
                            InputMappings.getKey("key.keyboard.left.shift").getDisplayName().getString(),
                            TextUtils.translateAsString(TRANSLATE_TYPE.TOOLTIP, "action_click")
                        ),
                        TextFormatting.AQUA
                    )
                )
                .append(" ")
                .append(TextUtils.translate(TRANSLATE_TYPE.TOOLTIP, key, TextFormatting.GRAY))
        );
    }
}
