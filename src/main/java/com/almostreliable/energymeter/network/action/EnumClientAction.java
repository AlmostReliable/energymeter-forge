package com.almostreliable.energymeter.network.action;

import com.almostreliable.energymeter.menu.SynchronizedContainerMenu;
import com.almostreliable.energymeter.network.action.ClientActionRegistry.Decoder;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.function.BiConsumer;

public final class EnumClientAction<M extends SynchronizedContainerMenu<?>, E extends Enum<E>> implements ClientAction<M> {

    private static final String VALUE_ID = "value";

    private final Identifier id;
    private final E value;
    private final BiConsumer<M, E> handler;

    public EnumClientAction(Identifier id, E value, BiConsumer<M, E> handler) {
        this.id = id;
        this.value = value;
        this.handler = handler;
    }

    @OnlyIn(Dist.CLIENT)
    public EnumClientAction(Identifier id, E value) {
        this(id, value, (a, b) -> {});
    }

    @Override
    public Identifier id() {
        return id;
    }

    @Override
    public void encode(CompoundTag tag) {
        tag.putInt(VALUE_ID, value.ordinal());
    }

    @Override
    public void handleServer(M menu, ServerPlayer player) {
        handler.accept(menu, value);
    }

    public static <M extends SynchronizedContainerMenu<?>, E extends Enum<E>> Decoder<EnumClientAction<M, E>> decoder(
        Identifier id, Class<E> enumClass, BiConsumer<M, E> handler
    ) {
        E[] enumValues = enumClass.getEnumConstants();

        return tag -> {
            int ordinal = tag.getIntOr(VALUE_ID, -1);
            if (ordinal < 0 || ordinal >= enumValues.length) {
                throw new IllegalStateException("invalid enum ordinal: " + ordinal);
            }

            return new EnumClientAction<>(id, enumValues[ordinal], handler);
        };
    }
}
