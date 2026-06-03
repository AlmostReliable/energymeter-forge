package com.almostreliable.energymeter.client.screen;

import com.almostreliable.energymeter.menu.MonitorMenu;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class MonitorScreen extends SynchronizedContainerScreen<MonitorMenu> {

    public MonitorScreen(MonitorMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title, 176, 166);
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {

    }
}
