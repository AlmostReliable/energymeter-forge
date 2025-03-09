package com.almostreliable.energymeter.network.packet;

import com.almostreliable.energymeter.menu.SynchronizedContainerMenu;
import com.almostreliable.energymeter.util.Utils;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public final class ClientActionPacket implements CustomPacketPayload {

    public static final Type<ClientActionPacket> TYPE = new Type<>(Utils.getRL("client_action"));
    public static final StreamCodec<FriendlyByteBuf, ClientActionPacket> STREAM_CODEC = CustomPacketPayload.codec(
        ClientActionPacket::encode,
        ClientActionPacket::decode
    );

    private final int wid;
    private final CompoundTag data;

    public ClientActionPacket(int wid, CompoundTag data) {
        this.wid = wid;
        this.data = data;
    }

    private static void encode(ClientActionPacket packet, FriendlyByteBuf buffer) {
        buffer.writeInt(packet.wid);
        buffer.writeNbt(packet.data);
    }

    private static ClientActionPacket decode(FriendlyByteBuf buffer) {
        int wid = buffer.readInt();
        CompoundTag data = buffer.readNbt();
        if (data == null) {
            data = new CompoundTag();
        }
        return new ClientActionPacket(wid, data);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(ClientActionPacket payload, IPayloadContext context) {
        if (context.player() instanceof ServerPlayer serverPlayer &&
            serverPlayer.containerMenu instanceof SynchronizedContainerMenu<?> menu &&
            menu.containerId == payload.wid) {
            menu.receiveClientData(serverPlayer, payload.data);
        }
    }
}
