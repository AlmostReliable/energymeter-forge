package com.github.almostreliable.energymeter.network;

import com.github.almostreliable.energymeter.core.Constants.SYNC_FLAGS;
import com.github.almostreliable.energymeter.meter.MeterEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.world.level.Level;

final class ClientHandler {

    private ClientHandler() {}

    @SuppressWarnings("StaticMethodOnlyUsedInOneClass")
    static void handleClientSyncPacket(ClientSyncPacket packet) {
        Level level = Minecraft.getInstance().level;
        if (level == null) return;
        var entity = level.getBlockEntity(packet.getPos());
        if (entity instanceof MeterEntity tile) {
            if ((packet.getFlags() & SYNC_FLAGS.SIDE_CONFIG) != 0) {
                tile.getSideConfig().deserializeNBT(packet.getSideConfig());
            }
            if ((packet.getFlags() & SYNC_FLAGS.TRANSFER_RATE) != 0) tile.setTransferRate(packet.getTransferRate());
            if ((packet.getFlags() & SYNC_FLAGS.NUMBER_MODE) != 0) tile.setNumberMode(packet.getNumberMode());
            if ((packet.getFlags() & SYNC_FLAGS.STATUS) != 0) tile.setStatus(packet.getStatus());
            if ((packet.getFlags() & SYNC_FLAGS.MODE) != 0) tile.setMode(packet.getMode());
            if ((packet.getFlags() & SYNC_FLAGS.ACCURACY) != 0) tile.setAccuracy(packet.getAccuracy());
            if ((packet.getFlags() & SYNC_FLAGS.INTERVAL) != 0) tile.setInterval(packet.getInterval());
            if ((packet.getFlags() & SYNC_FLAGS.THRESHOLD) != 0) tile.setThreshold(packet.getThreshold());
        }
    }
}
