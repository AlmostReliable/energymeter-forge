package dev.rlnt.energymeter.network;

import dev.rlnt.energymeter.component.SideConfiguration;
import dev.rlnt.energymeter.core.Constants;
import dev.rlnt.energymeter.core.Constants.SyncFlags;
import dev.rlnt.energymeter.util.TypeEnums.MODE;
import dev.rlnt.energymeter.util.TypeEnums.NUMBER_MODE;
import dev.rlnt.energymeter.util.TypeEnums.STATUS;
import java.util.Objects;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.fmllegacy.network.NetworkEvent.Context;

public class ClientSyncPacket {

    private BlockPos pos;
    private int flags;
    private CompoundTag sideConfig;
    private float transferRate;
    private STATUS status;
    private NUMBER_MODE numberMode;
    private MODE mode;

    public ClientSyncPacket(
        BlockPos pos,
        int flags,
        SideConfiguration sideConfig,
        float transferRate,
        STATUS status,
        NUMBER_MODE numberMode,
        MODE mode
    ) {
        this.pos = pos;
        this.flags = flags;
        this.sideConfig = sideConfig.serializeNBT();
        this.transferRate = transferRate;
        this.status = status;
        this.numberMode = numberMode;
        this.mode = mode;
    }

    private ClientSyncPacket() {}

    static ClientSyncPacket decode(FriendlyByteBuf buffer) {
        var packet = new ClientSyncPacket();
        packet.pos = buffer.readBlockPos();
        packet.flags = buffer.readInt();
        if ((packet.flags & SyncFlags.SIDE_CONFIG) != 0) packet.sideConfig = Objects.requireNonNull(buffer.readNbt());
        if ((packet.flags & SyncFlags.TRANSFER_RATE) != 0) packet.transferRate = buffer.readFloat();
        if ((packet.flags & SyncFlags.STATUS) != 0) packet.status = STATUS.values()[buffer.readInt()];
        if ((packet.flags & SyncFlags.NUMBER_MODE) != 0) packet.numberMode = NUMBER_MODE.values()[buffer.readInt()];
        if ((packet.flags & SyncFlags.MODE) != 0) packet.mode = MODE.values()[buffer.readInt()];
        return packet;
    }

    static void handle(ClientSyncPacket packet, Supplier<Context> context) {
        context.get().enqueueWork(() -> handlePacket(packet));
        context.get().setPacketHandled(true);
    }

    private static void handlePacket(ClientSyncPacket packet) {
        ClientHandler.handleClientSyncPacket(packet);
    }

    BlockPos getPos() {
        return pos;
    }

    int getFlags() {
        return flags;
    }

    CompoundTag getSideConfig() {
        return sideConfig;
    }

    float getTransferRate() {
        return transferRate;
    }

    STATUS getStatus() {
        return status;
    }

    NUMBER_MODE getNumberMode() {
        return numberMode;
    }

    public MODE getMode() {
        return mode;
    }

    void encode(FriendlyByteBuf buffer) {
        buffer.writeBlockPos(pos);
        buffer.writeInt(flags);
        if ((flags & Constants.SyncFlags.SIDE_CONFIG) != 0) buffer.writeNbt(sideConfig);
        if ((flags & Constants.SyncFlags.TRANSFER_RATE) != 0) buffer.writeFloat(transferRate);
        if ((flags & Constants.SyncFlags.STATUS) != 0) buffer.writeInt(status.ordinal());
        if ((flags & Constants.SyncFlags.NUMBER_MODE) != 0) buffer.writeInt(numberMode.ordinal());
        if ((flags & Constants.SyncFlags.MODE) != 0) buffer.writeInt(mode.ordinal());
    }
}
