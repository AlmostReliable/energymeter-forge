package dev.rlnt.energymeter.network;

import dev.rlnt.energymeter.core.Constants.SyncFlags;
import dev.rlnt.energymeter.meter.MeterContainer;
import dev.rlnt.energymeter.meter.MeterEntity;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraftforge.fmllegacy.network.NetworkEvent.Context;

public class IntervalUpdatePacket {

    private int value;

    public IntervalUpdatePacket(int value) {
        this.value = value;
    }

    private IntervalUpdatePacket() {}

    static IntervalUpdatePacket decode(FriendlyByteBuf buffer) {
        IntervalUpdatePacket packet = new IntervalUpdatePacket();
        packet.value = buffer.readInt();
        return packet;
    }

    static void handle(IntervalUpdatePacket packet, Supplier<Context> context) {
        ServerPlayer player = context.get().getSender();
        context.get().enqueueWork(() -> handlePacket(packet, player));
        context.get().setPacketHandled(true);
    }

    private static void handlePacket(IntervalUpdatePacket packet, @Nullable ServerPlayer player) {
        if (player != null && player.containerMenu instanceof MeterContainer) {
            MeterEntity tile = ((MeterContainer) player.containerMenu).getEntity();
            Level level = tile.getLevel();
            if (level == null || !level.isLoaded(tile.getBlockPos())) return;
            tile.setInterval(packet.value);
            tile.syncData(SyncFlags.INTERVAL);
            tile.setChanged();
        }
    }

    void encode(FriendlyByteBuf buffer) {
        buffer.writeInt(value);
    }
}
