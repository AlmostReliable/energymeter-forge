package com.github.almostreliable.energymeter.network;

import com.github.almostreliable.energymeter.util.TextUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;

import static com.github.almostreliable.energymeter.core.Constants.NETWORK_ID;

public enum PacketHandler {
    ;

    private static final ResourceLocation ID = TextUtils.getRL(NETWORK_ID);
    private static final String PROTOCOL = "1";
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(ID,
        () -> PROTOCOL,
        PROTOCOL::equals,
        PROTOCOL::equals
    );

    @SuppressWarnings("ValueOfIncrementOrDecrementUsed")
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

        CHANNEL
            .messageBuilder(IntervalUpdatePacket.class, ++id, NetworkDirection.PLAY_TO_SERVER)
            .decoder(IntervalUpdatePacket::decode)
            .encoder(IntervalUpdatePacket::encode)
            .consumer(IntervalUpdatePacket::handle)
            .add();
    }
}
