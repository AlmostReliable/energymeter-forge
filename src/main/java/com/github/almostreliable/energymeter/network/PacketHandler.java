package com.github.almostreliable.energymeter.network;

import com.github.almostreliable.energymeter.core.Constants;
import com.github.almostreliable.energymeter.network.packets.AccuracyUpdatePacket;
import com.github.almostreliable.energymeter.network.packets.ClientSyncPacket;
import com.github.almostreliable.energymeter.network.packets.IOUpdatePacket;
import com.github.almostreliable.energymeter.network.packets.SettingUpdatePacket;
import com.github.almostreliable.energymeter.util.TextUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public final class PacketHandler {

    private static final ResourceLocation ID = TextUtils.getRL(Constants.NETWORK_ID);
    private static final String PROTOCOL = "1";
    public static final SimpleChannel CHANNEL = NetworkRegistry.ChannelBuilder
        .named(ID)
        .networkProtocolVersion(() -> PROTOCOL)
        .clientAcceptedVersions(PROTOCOL::equals)
        .serverAcceptedVersions(PROTOCOL::equals)
        .simpleChannel();

    private PacketHandler() {}

    @SuppressWarnings("ValueOfIncrementOrDecrementUsed")
    public static void init() {
        var id = -1;

        // server to client
        register(++id, ClientSyncPacket.class, new ClientSyncPacket());
        // client to server
        register(++id, AccuracyUpdatePacket.class, new AccuracyUpdatePacket());
        register(++id, IOUpdatePacket.class, new IOUpdatePacket());
        register(++id, SettingUpdatePacket.class, new SettingUpdatePacket());
    }

    private static <T> void register(int packetId, Class<T> clazz, Packet<T> packet) {
        CHANNEL.registerMessage(packetId, clazz, packet::encode, packet::decode, packet::handle);
    }
}
