package com.almostreliable.energymeter.client.screen.widget;

import com.almostreliable.energymeter.EnergyMeter;
import com.almostreliable.energymeter.client.screen.MeterScreen.TabType;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Consumer;

public class TabButton extends AbstractWidget {

    private static final ResourceLocation TEXTURE = EnergyMeter.getRL("textures/gui/button/tab.png");
    private static final int TEXTURE_WIDTH = 45;
    private static final int TEXTURE_HEIGHT = 32;
    private static final int TAB_WIDTH = 25;
    public static final int TAB_HEIGHT = 19;
    private static final int ICON_WIDTH = 15;
    private static final int ICON_HEIGHT = 13;

    private final TabType tabType;
    private final boolean isSelected;
    private final Consumer<TabType> onClick;

    public TabButton(TabType tabType, TabType selected, Consumer<TabType> onClick) {
        super(0, 0, TAB_WIDTH, TAB_HEIGHT, Component.empty());
        this.tabType = tabType;
        this.isSelected = tabType == selected;
        this.onClick = onClick;
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        // tab background
        int tabHeight = isSelected ? TAB_HEIGHT : (TAB_HEIGHT - 1);
        guiGraphics.blit(TEXTURE, getX(), getY(), 0, 0, TAB_WIDTH, tabHeight, TEXTURE_WIDTH, TEXTURE_HEIGHT);

        // tab icon
        int uOffset = tabType.ordinal() * ICON_WIDTH;
        guiGraphics.blit(TEXTURE, getX() + 5, getY() + 3, uOffset, TAB_HEIGHT, ICON_WIDTH, ICON_HEIGHT, TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
        defaultButtonNarrationText(narrationElementOutput);
    }

    @Override
    public void playDownSound(SoundManager handler) {
        if (isSelected) return;
        super.playDownSound(handler);
    }

    @Override
    public void onClick(double mouseX, double mouseY, int button) {
        if (isSelected) return;
        onClick.accept(tabType);
    }
}
