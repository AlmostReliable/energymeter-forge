package com.github.almostreliable.energymeter.network;

import com.github.almostreliable.energymeter.component.SideConfiguration;
import com.github.almostreliable.energymeter.core.Constants.SYNC_FLAGS;
import com.github.almostreliable.energymeter.util.TypeEnums.ACCURACY;
import com.github.almostreliable.energymeter.util.TypeEnums.MODE;
import com.github.almostreliable.energymeter.util.TypeEnums.NUMBER_MODE;
import com.github.almostreliable.energymeter.util.TypeEnums.STATUS;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent.Context;

import java.util.Objects;
import java.util.function.Supplier;

public class ClientSyncPacket {

    private BlockPos pos;
    private int flags;
    private CompoundTag sideConfig;
    private double transferRate;
    private NUMBER_MODE numberMode;
    private STATUS status;
    private MODE mode;
    private ACCURACY accuracy;
    private int interval;

    @SuppressWarnings("java:S107")
    public ClientSyncPacket(
        BlockPos pos, int flags, SideConfiguration sideConfig, double transferRate, NUMBER_MODE numberMode,
        STATUS status, MODE mode, ACCURACY accuracy, int interval
    ) {
        this.pos = pos;
        this.flags = flags;
        this.sideConfig = sideConfig.serializeNBT();
        this.transferRate = transferRate;
        this.numberMode = numberMode;
        this.status = status;
        this.mode = mode;
        this.accuracy = accuracy;
        this.interval = interval;
    }

    private ClientSyncPacket() {}

    static ClientSyncPacket decode(FriendlyByteBuf buffer) {
        var packet = new ClientSyncPacket();
        packet.pos = buffer.readBlockPos();
        packet.flags = buffer.readInt();
        if ((packet.flags & SYNC_FLAGS.SIDE_CONFIG) != 0) packet.sideConfig = Objects.requireNonNull(buffer.readNbt());
        if ((packet.flags & SYNC_FLAGS.TRANSFER_RATE) != 0) packet.transferRate = buffer.readDouble();
        if ((packet.flags & SYNC_FLAGS.NUMBER_MODE) != 0) packet.numberMode = NUMBER_MODE.values()[buffer.readInt()];
        if ((packet.flags & SYNC_FLAGS.STATUS) != 0) packet.status = STATUS.values()[buffer.readInt()];
        if ((packet.flags & SYNC_FLAGS.MODE) != 0) packet.mode = MODE.values()[buffer.readInt()];
        if ((packet.flags & SYNC_FLAGS.ACCURACY) != 0) packet.accuracy = ACCURACY.values()[buffer.readInt()];
        if ((packet.flags & SYNC_FLAGS.INTERVAL) != 0) packet.interval = buffer.readInt();
        return packet;
    }

    static void handle(ClientSyncPacket packet, Supplier<? extends Context> context) {
        context.get().enqueueWork(() -> handlePacket(packet));
        context.get().setPacketHandled(true);
    }

    private static void handlePacket(ClientSyncPacket packet) {
        ClientHandler.handleClientSyncPacket(packet);
    }

    int getInterval() {
        return interval;
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

    double getTransferRate() {
        return transferRate;
    }

    NUMBER_MODE getNumberMode() {
        return numberMode;
    }

    STATUS getStatus() {
        return status;
    }

    public MODE getMode() {
        return mode;
    }

    ACCURACY getAccuracy() {
        return accuracy;
    }

    void encode(FriendlyByteBuf buffer) {
        buffer.writeBlockPos(pos);
        buffer.writeInt(flags);
        if ((flags & SYNC_FLAGS.SIDE_CONFIG) != 0) buffer.writeNbt(sideConfig);
        if ((flags & SYNC_FLAGS.TRANSFER_RATE) != 0) buffer.writeDouble(transferRate);
        if ((flags & SYNC_FLAGS.NUMBER_MODE) != 0) buffer.writeInt(numberMode.ordinal());
        if ((flags & SYNC_FLAGS.STATUS) != 0) buffer.writeInt(status.ordinal());
        if ((flags & SYNC_FLAGS.MODE) != 0) buffer.writeInt(mode.ordinal());
        if ((flags & SYNC_FLAGS.ACCURACY) != 0) buffer.writeInt(accuracy.ordinal());
        if ((flags & SYNC_FLAGS.INTERVAL) != 0) buffer.writeInt(interval);
    }
}
