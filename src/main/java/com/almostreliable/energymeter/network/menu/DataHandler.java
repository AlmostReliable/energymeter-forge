package com.almostreliable.energymeter.network.menu;

import net.minecraft.network.FriendlyByteBuf;

public interface DataHandler {

    void encode(FriendlyByteBuf buffer);

    void decode(FriendlyByteBuf buffer);

    boolean hasChanged();
}
