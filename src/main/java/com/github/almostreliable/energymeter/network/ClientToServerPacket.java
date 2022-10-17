package com.github.almostreliable.energymeter.network;

import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import javax.annotation.Nullable;
import java.util.function.Supplier;

public abstract class ClientToServerPacket<T> implements Packet<T> {

    @Override
    public void handle(T packet, Supplier<? extends NetworkEvent.Context> context) {
        var player = context.get().getSender();
        context.get().enqueueWork(() -> handlePacket(packet, player));
        context.get().setPacketHandled(true);
    }

    protected abstract void handlePacket(T packet, @Nullable ServerPlayer player);
}
