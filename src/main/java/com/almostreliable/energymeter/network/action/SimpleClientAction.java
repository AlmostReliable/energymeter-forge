package com.almostreliable.energymeter.network.action;

import com.almostreliable.energymeter.menu.SynchronizedContainerMenu;
import com.almostreliable.energymeter.network.action.ClientActionRegistry.Decoder;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.function.Consumer;

public final class SimpleClientAction<M extends SynchronizedContainerMenu<?>> implements ClientAction<M> {

    private final Identifier id;
    private final Consumer<M> handler;

    public SimpleClientAction(Identifier id, Consumer<M> handler) {
        this.id = id;
        this.handler = handler;
    }

    @OnlyIn(Dist.CLIENT)
    public SimpleClientAction(Identifier id) {
        this(id, $ -> {});
    }

    @Override
    public Identifier id() {
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
        Identifier id, Consumer<M> handler
    ) {
        return tag -> new SimpleClientAction<>(id, handler);
    }
}
