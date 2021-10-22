package dev.rlnt.energymeter.util;

import com.mojang.blaze3d.platform.InputConstants;
import dev.rlnt.energymeter.util.TypeEnums.TRANSLATE_TYPE;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;

public class Tooltip {

    private final List<Component> value = new ArrayList<>();

    private Tooltip() {}

    public static Tooltip builder() {
        return new Tooltip();
    }

    public List<Component> get() {
        return value;
    }

    /**
     * Adds a generic component to the tooltip.
     *
     * @param component the component to add
     * @return the instance of the tooltip
     */
    public Tooltip add(Component component) {
        value.add(component);
        return this;
    }

    /**
     * Adds a blank line to the tooltip.
     *
     * @return the instance of the tooltip
     */
    public Tooltip addBlankLine() {
        return add(new TextComponent(" "));
    }

    /**
     * Adds a header component to the tooltip.
     *
     * @param key the key of the translation
     * @return the instance of the tooltip
     */
    public Tooltip addHeader(String key) {
        return add(TextUtils.translate(TRANSLATE_TYPE.TOOLTIP, key, ChatFormatting.GOLD));
    }

    /**
     * Adds a description component to the tooltip.
     *
     * @param key the key of the translation
     * @return the instance of the tooltip
     */
    public Tooltip addDescription(String key) {
        return add(TextUtils.translate(TRANSLATE_TYPE.TOOLTIP, key, ChatFormatting.WHITE));
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
                .colorize("> ", ChatFormatting.GRAY)
                .append(
                    TextUtils.colorize(InputConstants.getKey(input).getDisplayName().getString(), ChatFormatting.AQUA)
                )
                .append(" ")
                .append(TextUtils.translate(TRANSLATE_TYPE.TOOLTIP, key, ChatFormatting.GRAY))
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
                .colorize("> ", ChatFormatting.GRAY)
                .append(TextUtils.translate(TRANSLATE_TYPE.TOOLTIP, "action_click", ChatFormatting.AQUA))
                .append(" ")
                .append(TextUtils.translate(TRANSLATE_TYPE.TOOLTIP, key, ChatFormatting.GRAY))
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
                .colorize("> ", ChatFormatting.GRAY)
                .append(
                    TextUtils.colorize(
                        String.format(
                            "%s + %s",
                            InputConstants.getKey("key.keyboard.left.shift").getDisplayName().getString(),
                            TextUtils.translateAsString(TRANSLATE_TYPE.TOOLTIP, "action_click")
                        ),
                        ChatFormatting.AQUA
                    )
                )
                .append(" ")
                .append(TextUtils.translate(TRANSLATE_TYPE.TOOLTIP, key, ChatFormatting.GRAY))
        );
    }
}
