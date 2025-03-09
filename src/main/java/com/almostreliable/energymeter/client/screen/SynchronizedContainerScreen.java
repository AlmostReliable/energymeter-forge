package com.almostreliable.energymeter.client.screen;

import com.almostreliable.energymeter.menu.SynchronizedContainerMenu;
import com.almostreliable.energymeter.network.packet.ClientActionPacket;

import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.network.PacketDistributor;

public abstract class SynchronizedContainerScreen<M extends SynchronizedContainerMenu<?>> extends AbstractContainerScreen<M> {

    SynchronizedContainerScreen(M menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
    }

    void sendAction(CompoundTag data) {
        PacketDistributor.sendToServer(new ClientActionPacket(menu.containerId, data));
    }
}
