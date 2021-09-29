package dev.rlnt.energymeter.network;

import static dev.rlnt.energymeter.core.Constants.MOD_ID;
import static dev.rlnt.energymeter.core.Constants.NETWORK_ID;

import dev.rlnt.energymeter.util.TextUtils;
import java.util.Objects;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;

public class PacketHandler {

    public static final SimpleChannel channel;
    private static final String ID = MOD_ID + "-" + NETWORK_ID;

    static {
        channel =
            NetworkRegistry.ChannelBuilder
                .named(TextUtils.getRL(NETWORK_ID))
                .clientAcceptedVersions(s -> Objects.equals(s, ID))
                .serverAcceptedVersions(s -> Objects.equals(s, ID))
                .networkProtocolVersion(() -> ID)
                .simpleChannel();

        int id = -1;

        channel
            .messageBuilder(IOUpdatePacket.class, ++id)
            .decoder(IOUpdatePacket::fromBytes)
            .encoder(IOUpdatePacket::toBytes)
            .consumer(IOUpdatePacket::handle)
            .add();

        channel
            .messageBuilder(SettingUpdatePacket.class, ++id)
            .decoder(SettingUpdatePacket::fromBytes)
            .encoder(SettingUpdatePacket::toBytes)
            .consumer(SettingUpdatePacket::handle)
            .add();
    }

    private PacketHandler() {
        throw new IllegalStateException("Utility class");
    }

    public static void init() {
        // utility method to initialize the packet handler
    }
}
