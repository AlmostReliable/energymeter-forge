package com.github.almostreliable.energymeter.network;

import com.github.almostreliable.energymeter.core.Constants.SYNC_FLAGS;
import com.github.almostreliable.energymeter.meter.MeterContainer;
import com.github.almostreliable.energymeter.util.TypeEnums.TEXT_BOX;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent.Context;

import javax.annotation.Nullable;
import java.util.function.Supplier;

public class AccuracyUpdatePacket {

    private TEXT_BOX identifier;
    private int value;

    public AccuracyUpdatePacket(TEXT_BOX identifier, int value) {
        this.identifier = identifier;
        this.value = value;
    }

    private AccuracyUpdatePacket() {}

    static AccuracyUpdatePacket decode(FriendlyByteBuf buffer) {
        var packet = new AccuracyUpdatePacket();
        packet.identifier = TEXT_BOX.values()[buffer.readInt()];
        packet.value = buffer.readInt();
        return packet;
    }

    static void handle(AccuracyUpdatePacket packet, Supplier<? extends Context> context) {
        var player = context.get().getSender();
        context.get().enqueueWork(() -> handlePacket(packet, player));
        context.get().setPacketHandled(true);
    }

    private static void handlePacket(AccuracyUpdatePacket packet, @Nullable ServerPlayer player) {
        if (player != null && player.containerMenu instanceof MeterContainer) {
            var entity = ((MeterContainer) player.containerMenu).getEntity();
            var level = entity.getLevel();
            if (level == null || !level.isLoaded(entity.getBlockPos())) return;
            if (packet.identifier == TEXT_BOX.INTERVAL) {
                entity.setInterval(packet.value);
                entity.syncData(SYNC_FLAGS.INTERVAL);
            } else if (packet.identifier == TEXT_BOX.THRESHOLD) {
                entity.setThreshold(packet.value);
                entity.syncData(SYNC_FLAGS.THRESHOLD);
            }
            entity.setChanged();
        }
    }

    void encode(FriendlyByteBuf buffer) {
        buffer.writeInt(identifier.ordinal());
        buffer.writeInt(value);
    }
}
