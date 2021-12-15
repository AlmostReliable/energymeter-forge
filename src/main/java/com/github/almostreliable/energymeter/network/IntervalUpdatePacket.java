package com.github.almostreliable.energymeter.network;

import com.github.almostreliable.energymeter.core.Constants.SYNC_FLAGS;
import com.github.almostreliable.energymeter.meter.MeterContainer;
import com.github.almostreliable.energymeter.meter.MeterTile;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkEvent.Context;

import javax.annotation.Nullable;
import java.util.function.Supplier;

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

    static void handle(IntervalUpdatePacket packet, Supplier<? extends Context> context) {
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
            tile.syncData(SYNC_FLAGS.INTERVAL);
            tile.setChanged();
        }
    }

    void encode(PacketBuffer buffer) {
        buffer.writeInt(value);
    }
}
