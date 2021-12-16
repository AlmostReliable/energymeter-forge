package com.github.almostreliable.energymeter.network;

import com.github.almostreliable.energymeter.core.Constants.SYNC_FLAGS;
import com.github.almostreliable.energymeter.meter.MeterContainer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent.Context;

import javax.annotation.Nullable;
import java.util.function.Supplier;

public class IntervalUpdatePacket {

    private int value;

    public IntervalUpdatePacket(int value) {
        this.value = value;
    }

    private IntervalUpdatePacket() {}

    static IntervalUpdatePacket decode(FriendlyByteBuf buffer) {
        var packet = new IntervalUpdatePacket();
        packet.value = buffer.readInt();
        return packet;
    }

    static void handle(IntervalUpdatePacket packet, Supplier<? extends Context> context) {
        var player = context.get().getSender();
        context.get().enqueueWork(() -> handlePacket(packet, player));
        context.get().setPacketHandled(true);
    }

    private static void handlePacket(IntervalUpdatePacket packet, @Nullable ServerPlayer player) {
        if (player != null && player.containerMenu instanceof MeterContainer) {
            var entity = ((MeterContainer) player.containerMenu).getEntity();
            var level = entity.getLevel();
            if (level == null || !level.isLoaded(entity.getBlockPos())) return;
            entity.setInterval(packet.value);
            entity.syncData(SYNC_FLAGS.INTERVAL);
            entity.setChanged();
        }
    }

    void encode(FriendlyByteBuf buffer) {
        buffer.writeInt(value);
    }
}
