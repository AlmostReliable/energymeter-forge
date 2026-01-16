package com.almostreliable.energymeter.util;

import com.almostreliable.energymeter.data.EnergyMeterLang;
import com.almostreliable.energymeter.mixin.WidgetTooltipMixin;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.FormattedCharSequence;

import java.util.ArrayList;
import java.util.List;

public final class TooltipBuilder {

    private final List<Component> components = new ArrayList<>();

    private TooltipBuilder() {}

    public static TooltipBuilder create() {
        return new TooltipBuilder();
    }

    private TooltipBuilder add(Component component) {
        components.add(component);
        return this;
    }

    public TooltipBuilder blankLine() {
        return add(Component.literal(" "));
    }

    public TooltipBuilder literal(String text) {
        return add(Component.literal(text));
    }

    public TooltipBuilder header(MutableComponent header) {
        return add(header.withStyle(ChatFormatting.GOLD));
    }

    public TooltipBuilder keyValue(MutableComponent key, MutableComponent value) {
        return add(key.withStyle(ChatFormatting.AQUA).append(": ").append(value.withStyle(ChatFormatting.WHITE)));
    }

    public TooltipBuilder lmbAction(MutableComponent action) {
        return add(
            EnergyMeterLang.LMB.get()
                .append(": ")
                .withStyle(ChatFormatting.GRAY)
                .append(action.withStyle(ChatFormatting.DARK_GRAY))
        );
    }

    public TooltipBuilder rmbAction(MutableComponent action) {
        return add(
            EnergyMeterLang.RMB.get()
                .append(": ")
                .withStyle(ChatFormatting.GRAY)
                .append(action.withStyle(ChatFormatting.DARK_GRAY))
        );
    }

    public TooltipBuilder shiftLmbAction(MutableComponent action) {
        return add(
            EnergyMeterLang.SHIFT.get()
                .append("-")
                .append(EnergyMeterLang.LMB.get())
                .append(": ")
                .withStyle(ChatFormatting.GRAY)
                .append(action.withStyle(ChatFormatting.DARK_GRAY))
        );
    }

    public Tooltip build() {
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
