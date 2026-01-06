package com.almostreliable.energymeter.network.menu.handler;

import com.almostreliable.energymeter.network.menu.AbstractDataHandler;

import net.minecraft.network.FriendlyByteBuf;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class LongDataHandler extends AbstractDataHandler<Long> {

    public LongDataHandler(Supplier<Long> getter, Consumer<Long> setter) {
        super(getter, setter);
    }

    @Override
    protected void handleEncoding(FriendlyByteBuf buffer, Long value) {
        buffer.writeLong(value);
    }

    @Override
    protected Long handleDecoding(FriendlyByteBuf buffer) {
        return buffer.readLong();
    }
}
