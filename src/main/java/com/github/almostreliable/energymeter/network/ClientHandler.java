package com.github.almostreliable.energymeter.network;

import com.github.almostreliable.energymeter.core.Constants.SYNC_FLAGS;
import com.github.almostreliable.energymeter.meter.MeterTile;
import net.minecraft.client.Minecraft;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

enum ClientHandler {
    ;

    @SuppressWarnings("StaticMethodOnlyUsedInOneClass")
    static void handleClientSyncPacket(ClientSyncPacket packet) {
        World level = Minecraft.getInstance().level;
        if (level == null) return;
        TileEntity tileEntity = level.getBlockEntity(packet.getPos());
        if (tileEntity instanceof MeterTile) {
            MeterTile tile = (MeterTile) tileEntity;
            if ((packet.getFlags() & SYNC_FLAGS.SIDE_CONFIG) != 0) {
                tile.getSideConfig().deserializeNBT(packet.getSideConfig());
            }
            if ((packet.getFlags() & SYNC_FLAGS.TRANSFER_RATE) != 0) {
                tile.setTransferRate(packet.getTransferRate());
            }
            if ((packet.getFlags() & SYNC_FLAGS.NUMBER_MODE) != 0) tile.setNumberMode(packet.getNumberMode());
            if ((packet.getFlags() & SYNC_FLAGS.STATUS) != 0) tile.setStatus(packet.getStatus());
            if ((packet.getFlags() & SYNC_FLAGS.MODE) != 0) tile.setMode(packet.getMode());
            if ((packet.getFlags() & SYNC_FLAGS.ACCURACY) != 0) tile.setAccuracy(packet.getAccuracy());
            if ((packet.getFlags() & SYNC_FLAGS.INTERVAL) != 0) tile.setInterval(packet.getInterval());
        }
    }
}
