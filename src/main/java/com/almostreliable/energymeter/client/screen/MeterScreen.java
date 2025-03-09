package com.almostreliable.energymeter.client.screen;

import com.almostreliable.energymeter.menu.MeterMenu;
import com.almostreliable.energymeter.util.TextUtils;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class MeterScreen extends SynchronizedContainerScreen<MeterMenu> {

    private static final ResourceLocation TEXTURE = TextUtils.getRL("textures/gui/meter.png");
    private static final int TEXTURE_WIDTH = 199;
    private static final int TEXTURE_HEIGHT = 129;

    public MeterScreen(MeterMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        guiGraphics.blit(TEXTURE, leftPos, topPos, 0, 0, TEXTURE_WIDTH, TEXTURE_HEIGHT, TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }
}
