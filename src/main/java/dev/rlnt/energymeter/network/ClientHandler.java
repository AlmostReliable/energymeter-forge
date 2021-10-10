package dev.rlnt.energymeter.network;

import dev.rlnt.energymeter.core.Constants;
import dev.rlnt.energymeter.meter.MeterTile;
import net.minecraft.client.Minecraft;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

class ClientHandler {

    private ClientHandler() {}

    static void handleClientSyncPacket(ClientSyncPacket packet) {
        World level = Minecraft.getInstance().level;
        if (level == null) return;
        TileEntity tileEntity = level.getBlockEntity(packet.getPos());
        if (tileEntity instanceof MeterTile) {
            MeterTile tile = (MeterTile) tileEntity;
            if ((packet.getFlags() & Constants.SyncFlags.SIDE_CONFIG) != 0) tile
                .getSideConfig()
                .deserializeNBT(packet.getSideConfig());
            if ((packet.getFlags() & Constants.SyncFlags.TRANSFER_RATE) != 0) tile.setTransferRate(
                packet.getTransferRate()
            );
            if ((packet.getFlags() & Constants.SyncFlags.STATUS) != 0) tile.setStatus(packet.getStatus());
            if ((packet.getFlags() & Constants.SyncFlags.NUMBER_MODE) != 0) tile.setNumberMode(packet.getNumberMode());
            if ((packet.getFlags() & Constants.SyncFlags.MODE) != 0) tile.setMode(packet.getMode());
        }
    }
}
