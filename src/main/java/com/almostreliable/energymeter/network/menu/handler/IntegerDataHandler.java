package com.almostreliable.energymeter.network.menu.handler;

import com.almostreliable.energymeter.network.menu.AbstractDataHandler;

import net.minecraft.network.FriendlyByteBuf;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class IntegerDataHandler extends AbstractDataHandler<Integer> {

    public IntegerDataHandler(Supplier<Integer> getter, Consumer<Integer> setter) {
        super(getter, setter);
    }

    @Override
    protected void handleEncoding(FriendlyByteBuf buffer, Integer value) {
        buffer.writeInt(value);
    }

    @Override
    protected Integer handleDecoding(FriendlyByteBuf buffer) {
        return buffer.readInt();
    }
}
