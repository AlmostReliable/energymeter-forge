package dev.rlnt.energymeter.network;

import dev.rlnt.energymeter.core.Constants.SyncFlags;
import dev.rlnt.energymeter.meter.MeterContainer;
import dev.rlnt.energymeter.meter.MeterTile;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkEvent;

public class IntervalUpdatePacket {

    private int value;

    public IntervalUpdatePacket(int value) {
        this.value = value;
    }

    private IntervalUpdatePacket() {}

    static IntervalUpdatePacket decode(PacketBuffer buffer) {
        IntervalUpdatePacket packet = new IntervalUpdatePacket();
        packet.value = buffer.readInt();
        return packet;
    }

    static void handle(IntervalUpdatePacket packet, Supplier<NetworkEvent.Context> context) {
        ServerPlayerEntity player = context.get().getSender();
        context.get().enqueueWork(() -> handlePacket(packet, player));
        context.get().setPacketHandled(true);
    }

    private static void handlePacket(IntervalUpdatePacket packet, @Nullable ServerPlayerEntity player) {
        if (player != null && player.containerMenu instanceof MeterContainer) {
            MeterTile tile = ((MeterContainer) player.containerMenu).getTile();
            World level = tile.getLevel();
            if (level == null || !level.isLoaded(tile.getBlockPos())) return;
            tile.setInterval(packet.value);
            tile.syncData(SyncFlags.INTERVAL);
            tile.setChanged();
        }
    }

    void encode(PacketBuffer buffer) {
        buffer.writeInt(value);
    }
}
