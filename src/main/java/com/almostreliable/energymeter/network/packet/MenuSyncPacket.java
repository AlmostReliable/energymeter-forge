package com.almostreliable.energymeter.network.packet;

import com.almostreliable.energymeter.EnergyMeter;
import com.almostreliable.energymeter.menu.SynchronizedContainerMenu;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import io.netty.buffer.Unpooled;

import java.util.function.Consumer;

public final class MenuSyncPacket implements CustomPacketPayload {

    public static final Type<MenuSyncPacket> TYPE = new Type<>(EnergyMeter.getRL("menu_sync"));
    public static final StreamCodec<FriendlyByteBuf, MenuSyncPacket> STREAM_CODEC = CustomPacketPayload.codec(
        MenuSyncPacket::encode,
        MenuSyncPacket::decode
    );

    private final int wid;
    private final FriendlyByteBuf data;

    private MenuSyncPacket(int wid, FriendlyByteBuf data) {
        this.wid = wid;
        this.data = data;
    }

    public static MenuSyncPacket of(int wid, Consumer<FriendlyByteBuf> consumer) {
        FriendlyByteBuf data = new FriendlyByteBuf(Unpooled.buffer());
        consumer.accept(data);
        return new MenuSyncPacket(wid, data);
    }

    private static void encode(MenuSyncPacket packet, FriendlyByteBuf buffer) {
        buffer.writeInt(packet.wid);
        buffer.writeBytes(packet.data.copy());
    }

    private static MenuSyncPacket decode(FriendlyByteBuf buffer) {
        int wid = buffer.readInt();
        int size = buffer.readableBytes();
        FriendlyByteBuf data = new FriendlyByteBuf(buffer.readBytes(size));
        return new MenuSyncPacket(wid, data);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(MenuSyncPacket payload, IPayloadContext context) {
        Player player = context.player();
        if (player.containerMenu instanceof SynchronizedContainerMenu<?> menu && menu.containerId == payload.wid) {
            menu.receiveServerData(payload.data);
        }
    }
}
