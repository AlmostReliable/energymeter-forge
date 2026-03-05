package com.almostreliable.energymeter.client.screen.widget;

import com.almostreliable.energymeter.EnergyMeter;
import com.almostreliable.energymeter.core.Constants;
import com.almostreliable.energymeter.data.EnergyMeterLang;
import com.almostreliable.energymeter.util.TooltipBuilder;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;

public class GuideButton extends AbstractButton {

    private static final int SIZE = 16;
    private static final Component TOOLTIP = EnergyMeter.isModLoaded(Constants.GUIDE_ME) ?
        EnergyMeterLang.BUTTON_GUIDE_OPEN_TOOLTIP.get() :
        EnergyMeterLang.BUTTON_GUIDE_MISSING_TOOLTIP.get();

    public GuideButton(int x, int y) {
        super(x, y, SIZE, SIZE, Component.literal("?"));
        setTooltip(TooltipBuilder.create().literal(TOOLTIP).build());
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        renderString(guiGraphics, Minecraft.getInstance().font, Constants.COLOR_WHITE);
    }

    @Override
    public void onPress() {
        var player = Minecraft.getInstance().player;
        if (player == null) return;
        player.connection.sendCommand(Constants.GUIDE_ME + "c energymeter:guide open energymeter:interface.md");
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
        defaultButtonNarrationText(narrationElementOutput);
    }
}
