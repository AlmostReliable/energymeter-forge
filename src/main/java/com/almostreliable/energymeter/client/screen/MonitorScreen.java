package com.almostreliable.energymeter.client.screen;

import com.almostreliable.energymeter.menu.MonitorMenu;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class MonitorScreen extends SynchronizedContainerScreen<MonitorMenu> {

    private static final int GUI_WIDTH = 176;
    private static final int GUI_HEIGHT = 166;

    public MonitorScreen(MonitorMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title, GUI_WIDTH, GUI_HEIGHT);
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        // wip
    }
}
