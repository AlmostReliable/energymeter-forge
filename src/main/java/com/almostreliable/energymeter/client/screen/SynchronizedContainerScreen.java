package com.almostreliable.energymeter.client.screen;

import com.almostreliable.energymeter.menu.SynchronizedContainerMenu;
import com.almostreliable.energymeter.network.action.ClientAction;
import com.almostreliable.energymeter.network.action.ClientActionRegistry;
import com.almostreliable.energymeter.network.packet.ClientActionPacket;

import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

public abstract class SynchronizedContainerScreen<M extends SynchronizedContainerMenu<?>> extends AbstractContainerScreen<M> {

    protected SynchronizedContainerScreen(M menu, Inventory playerInventory, Component title, int imageWidth, int imageHeight) {
        super(menu, playerInventory, title, imageWidth, imageHeight);
    }

    protected void sendClientAction(ClientAction<?> action) {
        var data = ClientActionRegistry.encode(action);
        ClientPacketDistributor.sendToServer(new ClientActionPacket(menu.containerId, data));
    }
}
