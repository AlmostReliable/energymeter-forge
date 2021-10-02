package dev.rlnt.energymeter.network;

import static dev.rlnt.energymeter.core.Constants.NETWORK_ID;

import dev.rlnt.energymeter.util.TextUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;

public class PacketHandler {

    private static final ResourceLocation ID = TextUtils.getRL(NETWORK_ID);
    private static final String PROTOCOL = "1";
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
        ID,
        () -> PROTOCOL,
        PROTOCOL::equals,
        PROTOCOL::equals
    );

    static {
        int id = -1;

        CHANNEL
            .messageBuilder(IOUpdatePacket.class, ++id)
            .decoder(IOUpdatePacket::decode)
            .encoder(IOUpdatePacket::encode)
            .consumer(IOUpdatePacket::handle)
            .add();

        CHANNEL
            .messageBuilder(SettingUpdatePacket.class, ++id)
            .decoder(SettingUpdatePacket::decode)
            .encoder(SettingUpdatePacket::encode)
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
