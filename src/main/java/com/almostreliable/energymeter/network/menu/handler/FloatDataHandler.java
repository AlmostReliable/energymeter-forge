package com.almostreliable.energymeter.network.menu.handler;

import com.almostreliable.energymeter.network.menu.AbstractDataHandler;

import net.minecraft.network.FriendlyByteBuf;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class FloatDataHandler extends AbstractDataHandler<Float> {

    public FloatDataHandler(Supplier<Float> getter, Consumer<Float> setter) {
        super(getter, setter);
    }

    @Override
    protected void handleEncoding(FriendlyByteBuf buffer, Float value) {
        buffer.writeFloat(value);
    }

    @Override
    protected Float handleDecoding(FriendlyByteBuf buffer) {
        return buffer.readFloat();
    }
}
