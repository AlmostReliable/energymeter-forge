package dev.rlnt.energymeter.network;

import dev.rlnt.energymeter.meter.MeterTile;
import dev.rlnt.energymeter.meter.SideConfiguration;
import dev.rlnt.energymeter.network.PacketHandler.SyncFlags;
import dev.rlnt.energymeter.util.TypeEnums.MODE;
import dev.rlnt.energymeter.util.TypeEnums.NUMBER_MODE;
import dev.rlnt.energymeter.util.TypeEnums.STATUS;
import java.util.function.Supplier;
import net.minecraft.client.Minecraft;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkEvent;

public class ClientSyncPacket {

    private BlockPos pos;
    private int flags;
    private int[] sideConfig;
    private float transferRate;
    private STATUS status;
    private NUMBER_MODE numberMode;
    private MODE mode;

    public ClientSyncPacket(
        final BlockPos pos,
        final int flags,
        final SideConfiguration sideConfig,
        final float transferRate,
        final STATUS status,
        final NUMBER_MODE numberMode,
        final MODE mode
    ) {
        this.pos = pos;
        this.flags = flags;
        this.sideConfig = sideConfig.serialize();
        this.transferRate = transferRate;
        this.status = status;
        this.numberMode = numberMode;
        this.mode = mode;
    }

    private ClientSyncPacket() {}

    static ClientSyncPacket decode(final PacketBuffer buffer) {
        final ClientSyncPacket packet = new ClientSyncPacket();
        packet.pos = buffer.readBlockPos();
        packet.flags = buffer.readInt();
        if ((packet.flags & SyncFlags.SIDE_CONFIG) != 0) packet.sideConfig = buffer.readVarIntArray();
        if ((packet.flags & SyncFlags.TRANSFER_RATE) != 0) packet.transferRate = buffer.readFloat();
        if ((packet.flags & SyncFlags.STATUS) != 0) packet.status = STATUS.values()[buffer.readInt()];
        if ((packet.flags & SyncFlags.NUMBER_MODE) != 0) packet.numberMode = NUMBER_MODE.values()[buffer.readInt()];
        if ((packet.flags & SyncFlags.MODE) != 0) packet.mode = MODE.values()[buffer.readInt()];
        return packet;
    }

    static void handle(final ClientSyncPacket packet, final Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> handlePacket(packet));
        context.get().setPacketHandled(true);
    }

    private static void handlePacket(final ClientSyncPacket packet) {
        World level = Minecraft.getInstance().level;
        if (level == null) return;
        TileEntity tileEntity = level.getBlockEntity(packet.pos);
        if (tileEntity instanceof MeterTile) {
            final MeterTile tile = (MeterTile) tileEntity;
            if ((packet.flags & SyncFlags.SIDE_CONFIG) != 0) tile.getSideConfig().deserialize(packet.sideConfig);
            if ((packet.flags & SyncFlags.TRANSFER_RATE) != 0) tile.setTransferRate(packet.transferRate);
            if ((packet.flags & SyncFlags.STATUS) != 0) tile.setStatus(packet.status);
            if ((packet.flags & SyncFlags.NUMBER_MODE) != 0) tile.setNumberMode(packet.numberMode);
            if ((packet.flags & SyncFlags.MODE) != 0) tile.setMode(packet.mode);
        }
    }

    void encode(final PacketBuffer buffer) {
        buffer.writeBlockPos(pos);
        buffer.writeInt(flags);
        if ((flags & SyncFlags.SIDE_CONFIG) != 0) buffer.writeVarIntArray(sideConfig);
        if ((flags & SyncFlags.TRANSFER_RATE) != 0) buffer.writeFloat(transferRate);
        if ((flags & SyncFlags.STATUS) != 0) buffer.writeInt(status.ordinal());
        if ((flags & SyncFlags.NUMBER_MODE) != 0) buffer.writeInt(numberMode.ordinal());
        if ((flags & SyncFlags.MODE) != 0) buffer.writeInt(mode.ordinal());
    }
}
