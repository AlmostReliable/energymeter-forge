package com.github.almostreliable.energymeter.network;

import com.github.almostreliable.energymeter.core.Constants.SYNC_FLAGS;
import com.github.almostreliable.energymeter.meter.MeterContainer;
import com.github.almostreliable.energymeter.meter.MeterTile;
import com.github.almostreliable.energymeter.util.TypeEnums.TEXT_BOX;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkEvent.Context;

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

    static AccuracyUpdatePacket decode(PacketBuffer buffer) {
        AccuracyUpdatePacket packet = new AccuracyUpdatePacket();
        packet.identifier = TEXT_BOX.values()[buffer.readInt()];
        packet.value = buffer.readInt();
        return packet;
    }

    static void handle(AccuracyUpdatePacket packet, Supplier<? extends Context> context) {
        ServerPlayerEntity player = context.get().getSender();
        context.get().enqueueWork(() -> handlePacket(packet, player));
        context.get().setPacketHandled(true);
    }

    private static void handlePacket(AccuracyUpdatePacket packet, @Nullable ServerPlayerEntity player) {
        if (player != null && player.containerMenu instanceof MeterContainer) {
            MeterTile tile = ((MeterContainer) player.containerMenu).getTile();
            World level = tile.getLevel();
            if (level == null || !level.isLoaded(tile.getBlockPos())) return;
            if (packet.identifier == TEXT_BOX.INTERVAL) {
                tile.setInterval(packet.value);
                tile.syncData(SYNC_FLAGS.INTERVAL);
            } else if (packet.identifier == TEXT_BOX.THRESHOLD) {
                tile.setThreshold(packet.value);
                tile.syncData(SYNC_FLAGS.THRESHOLD);
            }
            tile.setChanged();
        }
    }

    void encode(PacketBuffer buffer) {
        buffer.writeInt(identifier.ordinal());
        buffer.writeInt(value);
    }
}
