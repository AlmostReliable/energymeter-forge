package com.almostreliable.energymeter.mixin;

import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.util.FormattedCharSequence;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(Tooltip.class)
public interface WidgetTooltipMixin {

    @Accessor("cachedTooltip")
    void setCachedTooltip(List<FormattedCharSequence> cachedTooltip);
}
