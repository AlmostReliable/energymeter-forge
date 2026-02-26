package com.almostreliable.energymeter.network.action;

import com.almostreliable.energymeter.menu.SynchronizedContainerMenu;
import com.almostreliable.energymeter.network.action.ClientActionRegistry.Decoder;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.function.Consumer;

public final class SimpleClientAction<M extends SynchronizedContainerMenu<?>> implements ClientAction<M> {

    private final ResourceLocation id;
    private final Consumer<M> handler;

    public SimpleClientAction(ResourceLocation id, Consumer<M> handler) {
        this.id = id;
        this.handler = handler;
    }

    @OnlyIn(Dist.CLIENT)
    public SimpleClientAction(ResourceLocation id) {
        this(id, $ -> {});
    }

    @Override
    public ResourceLocation id() {
        return id;
    }

    @Override
    public void encode(CompoundTag tag) {
        // no-op
    }

    @Override
    public void handleServer(M menu, ServerPlayer player) {
        handler.accept(menu);
    }

    public static <M extends SynchronizedContainerMenu<?>> Decoder<SimpleClientAction<M>> decoder(
        ResourceLocation id, Consumer<M> handler
    ) {
        return tag -> new SimpleClientAction<>(id, handler);
    }
}
