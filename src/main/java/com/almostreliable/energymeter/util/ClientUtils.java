package com.almostreliable.energymeter.util;

import com.almostreliable.energymeter.mixin.WidgetTooltipMixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;

import java.util.ArrayList;
import java.util.List;

public final class ClientUtils {

    private ClientUtils() {}

    public static Tooltip createTooltip(List<Component> components) {
        Minecraft mc = Minecraft.getInstance();
        List<FormattedCharSequence> formatted = new ArrayList<>();
        for (Component component : components) {
            formatted.addAll(Tooltip.splitTooltip(mc, component));
        }

        Tooltip tooltip = Tooltip.create(Component.empty());
        tooltip.toCharSequence(mc);
        ((WidgetTooltipMixin) tooltip).setCachedTooltip(formatted);

        return tooltip;
    }
}
