package com.almostreliable.energymeter.network.menu.handler;

import com.almostreliable.energymeter.network.menu.AbstractDataHandler;

import net.minecraft.network.FriendlyByteBuf;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class BooleanDataHandler extends AbstractDataHandler<Boolean> {

    public BooleanDataHandler(Supplier<Boolean> getter, Consumer<Boolean> setter) {
        super(getter, setter);
    }

    @Override
    protected void handleEncoding(FriendlyByteBuf buffer, Boolean value) {
        buffer.writeBoolean(value);
    }

    @Override
    protected Boolean handleDecoding(FriendlyByteBuf buffer) {
        return buffer.readBoolean();
    }
}
