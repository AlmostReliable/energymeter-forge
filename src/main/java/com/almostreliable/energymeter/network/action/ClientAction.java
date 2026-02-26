package com.almostreliable.energymeter.network.action;

import com.almostreliable.energymeter.menu.SynchronizedContainerMenu;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

public interface ClientAction<M extends SynchronizedContainerMenu<?>> {

    ResourceLocation id();

    void encode(CompoundTag data);

    void handleServer(M menu, ServerPlayer player);
}
