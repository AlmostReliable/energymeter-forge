package com.github.almostreliable.energymeter.client.screen;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

import com.github.almostreliable.energymeter.menu.ScreenMenu;

public class ScreenScreen extends AbstractContainerScreen<ScreenMenu> {

    public ScreenScreen(ScreenMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {

    }
}
