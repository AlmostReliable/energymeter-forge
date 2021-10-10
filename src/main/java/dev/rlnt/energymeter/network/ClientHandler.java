package dev.rlnt.energymeter.network;

import dev.rlnt.energymeter.core.Constants;
import dev.rlnt.energymeter.meter.MeterEntity;
import net.minecraft.client.Minecraft;

class ClientHandler {

    private ClientHandler() {}

    static void handleClientSyncPacket(ClientSyncPacket packet) {
        var level = Minecraft.getInstance().level;
        if (level == null) return;
        if (level.getBlockEntity(packet.getPos()) instanceof MeterEntity entity) {
            if ((packet.getFlags() & Constants.SyncFlags.SIDE_CONFIG) != 0) entity
                .getSideConfig()
                .deserializeNBT(packet.getSideConfig());
            if ((packet.getFlags() & Constants.SyncFlags.TRANSFER_RATE) != 0) entity.setTransferRate(
                packet.getTransferRate()
            );
            if ((packet.getFlags() & Constants.SyncFlags.STATUS) != 0) entity.setStatus(packet.getStatus());
            if ((packet.getFlags() & Constants.SyncFlags.NUMBER_MODE) != 0) entity.setNumberMode(
                packet.getNumberMode()
            );
            if ((packet.getFlags() & Constants.SyncFlags.MODE) != 0) entity.setMode(packet.getMode());
        }
    }
}
