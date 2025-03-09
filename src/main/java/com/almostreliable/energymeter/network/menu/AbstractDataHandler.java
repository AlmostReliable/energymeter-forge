package com.almostreliable.energymeter.network.menu;

import net.minecraft.network.FriendlyByteBuf;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

public abstract class AbstractDataHandler<T> implements DataHandler {

    private final Supplier<T> getter;
    private final Consumer<T> setter;
    private T previousValue;

    protected AbstractDataHandler(Supplier<T> getter, Consumer<T> setter) {
        this.getter = getter;
        this.setter = setter;
        previousValue = getter.get();
    }

    @Override
    public void encode(FriendlyByteBuf buffer) {
        var currentValue = getter.get();
        previousValue = currentValue;
        handleEncoding(buffer, currentValue);
    }

    @Override
    public void decode(FriendlyByteBuf buffer) {
        setter.accept(handleDecoding(buffer));
    }

    @Override
    public boolean hasChanged() {
        return !Objects.equals(previousValue, getter.get());
    }

    protected abstract void handleEncoding(FriendlyByteBuf buffer, T value);

    protected abstract T handleDecoding(FriendlyByteBuf buffer);
}
