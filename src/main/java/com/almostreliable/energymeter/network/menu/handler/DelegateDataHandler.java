package com.almostreliable.energymeter.network.menu.handler;

import com.almostreliable.energymeter.network.menu.DataHandler;

import net.minecraft.network.FriendlyByteBuf;

import java.util.function.Supplier;

public class DelegateDataHandler implements DataHandler {

    private final DataHandler serverDelegate;
    private final Supplier<DataHandler> clientDelegate;

    public DelegateDataHandler(DataHandler serverDelegate, Supplier<DataHandler> clientDelegate) {
        this.serverDelegate = serverDelegate;
        this.clientDelegate = clientDelegate;
    }

    @Override
    public void encode(FriendlyByteBuf buffer) {
        serverDelegate.encode(buffer);
    }

    @Override
    public void decode(FriendlyByteBuf buffer) {
        clientDelegate.get().decode(buffer);
    }

    @Override
    public boolean hasChanged() {
        return serverDelegate.hasChanged();
    }
}
