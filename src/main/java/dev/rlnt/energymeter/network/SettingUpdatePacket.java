package dev.rlnt.energymeter.network;

import dev.rlnt.energymeter.meter.MeterContainer;
import dev.rlnt.energymeter.meter.MeterEntity;
import dev.rlnt.energymeter.util.TypeEnums.SETTING;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.fmllegacy.network.NetworkEvent.Context;

public class SettingUpdatePacket {

    private SETTING setting;

    public SettingUpdatePacket(final SETTING setting) {
        this.setting = setting;
    }

    private SettingUpdatePacket() {}

    static SettingUpdatePacket decode(final FriendlyByteBuf buffer) {
        final SettingUpdatePacket packet = new SettingUpdatePacket();
        packet.setting = SETTING.values()[buffer.readInt()];
        return packet;
    }

    static void handle(final SettingUpdatePacket packet, final Supplier<Context> context) {
        final ServerPlayer player = context.get().getSender();
        context.get().enqueueWork(() -> handlePacket(packet, player));
        context.get().setPacketHandled(true);
    }

    private static void handlePacket(final SettingUpdatePacket packet, @Nullable final ServerPlayer player) {
        if (player != null && player.containerMenu instanceof MeterContainer menu) {
            final MeterEntity tile = menu.getEntity();
            tile.updateSetting(packet.setting);
            tile.update(false);
            tile.setChanged();
        }
    }

    void encode(final FriendlyByteBuf buffer) {
        buffer.writeInt(setting.ordinal());
    }
}
