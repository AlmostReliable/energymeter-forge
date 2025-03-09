package com.almostreliable.energymeter.network;

import com.almostreliable.energymeter.block.entity.MeterBlockEntity;
import com.almostreliable.energymeter.core.Constants.SyncFlags;
import com.almostreliable.energymeter.util.TypeEnums.DisplayMode;
import com.almostreliable.energymeter.util.TypeEnums.MeasureMode;
import com.almostreliable.energymeter.util.TypeEnums.Status;
import com.almostreliable.energymeter.util.TypeEnums.TransferMode;
import com.almostreliable.energymeter.util.Utils;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import com.github.almostreliable.energymeter.block.component.SideConfigurationOld;

import java.util.Objects;

public class ClientSyncPacket implements CustomPacketPayload {

    static final Type<ClientSyncPacket> TYPE = new Type<>(Utils.getRL("client_sync"));
    static final StreamCodec<FriendlyByteBuf, ClientSyncPacket> STREAM_CODEC = CustomPacketPayload.codec(
        ClientSyncPacket::encode,
        ClientSyncPacket::decode
    );

    private BlockPos pos;
    private int flags;
    private CompoundTag sideConfig;
    private double transferRate;
    private DisplayMode numberMode;
    private Status status;
    private TransferMode mode;
    private MeasureMode accuracy;
    private int interval;
    private int threshold;

    @SuppressWarnings("java:S107")
    public ClientSyncPacket(
        BlockPos pos, int flags, SideConfigurationOld sideConfig, double transferRate, DisplayMode numberMode,
        Status status, TransferMode mode, MeasureMode accuracy, int interval, int threshold
    ) {
        this.pos = pos;
        this.flags = flags;
        this.sideConfig = sideConfig.serializeNBT(null); // TODO: Check if this is valid
        this.transferRate = transferRate;
        this.numberMode = numberMode;
        this.status = status;
        this.mode = mode;
        this.accuracy = accuracy;
        this.interval = interval;
        this.threshold = threshold;
    }

    public ClientSyncPacket() {}

    public static void encode(ClientSyncPacket packet, FriendlyByteBuf buffer) {
        buffer.writeBlockPos(packet.pos);
        buffer.writeInt(packet.flags);
        if ((packet.flags & SyncFlags.SIDE_CONFIG) != 0) buffer.writeNbt(packet.sideConfig);
        if ((packet.flags & SyncFlags.TRANSFER_RATE) != 0) buffer.writeDouble(packet.transferRate);
        if ((packet.flags & SyncFlags.NUMBER_MODE) != 0) buffer.writeInt(packet.numberMode.ordinal());
        if ((packet.flags & SyncFlags.STATUS) != 0) buffer.writeInt(packet.status.ordinal());
        if ((packet.flags & SyncFlags.MODE) != 0) buffer.writeInt(packet.mode.ordinal());
        if ((packet.flags & SyncFlags.ACCURACY) != 0) buffer.writeInt(packet.accuracy.ordinal());
        if ((packet.flags & SyncFlags.INTERVAL) != 0) buffer.writeInt(packet.interval);
        if ((packet.flags & SyncFlags.THRESHOLD) != 0) buffer.writeInt(packet.threshold);
    }

    public static ClientSyncPacket decode(FriendlyByteBuf buffer) {
        var packet = new ClientSyncPacket();
        packet.pos = buffer.readBlockPos();
        packet.flags = buffer.readInt();
        if ((packet.flags & SyncFlags.SIDE_CONFIG) != 0) packet.sideConfig = Objects.requireNonNull(buffer.readNbt());
        if ((packet.flags & SyncFlags.TRANSFER_RATE) != 0) packet.transferRate = buffer.readDouble();
        if ((packet.flags & SyncFlags.NUMBER_MODE) != 0) packet.numberMode = DisplayMode.values()[buffer.readInt()];
        if ((packet.flags & SyncFlags.STATUS) != 0) packet.status = Status.values()[buffer.readInt()];
        if ((packet.flags & SyncFlags.MODE) != 0) packet.mode = TransferMode.values()[buffer.readInt()];
        if ((packet.flags & SyncFlags.ACCURACY) != 0) packet.accuracy = MeasureMode.values()[buffer.readInt()];
        if ((packet.flags & SyncFlags.INTERVAL) != 0) packet.interval = buffer.readInt();
        if ((packet.flags & SyncFlags.THRESHOLD) != 0) packet.threshold = buffer.readInt();
        return packet;
    }

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(ClientSyncPacket payload, IPayloadContext context) {
        Player player = context.player();
        if (player != null) {
            var entity = player.level().getBlockEntity(payload.pos);
            if (entity instanceof MeterBlockEntity tile) {
                if ((payload.flags & SyncFlags.SIDE_CONFIG) != 0) {
                    tile.getSideConfig().deserializeNBT(player.registryAccess(), payload.sideConfig);
                }
                if ((payload.flags & SyncFlags.TRANSFER_RATE) != 0) tile.setEnergyRate(payload.transferRate);
                if ((payload.flags & SyncFlags.NUMBER_MODE) != 0) tile.setNumberMode(payload.numberMode);
                if ((payload.flags & SyncFlags.STATUS) != 0) tile.setStatus(payload.status);
                if ((payload.flags & SyncFlags.MODE) != 0) tile.setMode(payload.mode);
                if ((payload.flags & SyncFlags.ACCURACY) != 0) tile.setAccuracy(payload.accuracy);
                if ((payload.flags & SyncFlags.INTERVAL) != 0) tile.setInterval(payload.interval);
                if ((payload.flags & SyncFlags.THRESHOLD) != 0) tile.setThreshold(payload.threshold);
            }
        }
    }
}
