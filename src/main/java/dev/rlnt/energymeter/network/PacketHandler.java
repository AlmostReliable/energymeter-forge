package dev.rlnt.energymeter.network;

import static dev.rlnt.energymeter.core.Constants.NETWORK_ID;

import dev.rlnt.energymeter.util.TextUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fmllegacy.network.NetworkDirection;
import net.minecraftforge.fmllegacy.network.NetworkRegistry;
import net.minecraftforge.fmllegacy.network.simple.SimpleChannel;

public class PacketHandler {

    private static final ResourceLocation ID = TextUtils.getRL(NETWORK_ID);
    private static final String PROTOCOL = "1";
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
        ID,
        () -> PROTOCOL,
        PROTOCOL::equals,
        PROTOCOL::equals
    );

    private PacketHandler() {}

    public static void init() {
        int id = -1;

        CHANNEL
            .messageBuilder(ClientSyncPacket.class, ++id, NetworkDirection.PLAY_TO_CLIENT)
            .decoder(ClientSyncPacket::decode)
            .encoder(ClientSyncPacket::encode)
            .consumer(ClientSyncPacket::handle)
            .add();

        CHANNEL
            .messageBuilder(IOUpdatePacket.class, ++id, NetworkDirection.PLAY_TO_SERVER)
            .decoder(IOUpdatePacket::decode)
            .encoder(IOUpdatePacket::encode)
            .consumer(IOUpdatePacket::handle)
            .add();

        CHANNEL
            .messageBuilder(SettingUpdatePacket.class, ++id, NetworkDirection.PLAY_TO_SERVER)
            .decoder(SettingUpdatePacket::decode)
            .encoder(SettingUpdatePacket::encode)
            .consumer(SettingUpdatePacket::handle)
            .add();
    }
}
