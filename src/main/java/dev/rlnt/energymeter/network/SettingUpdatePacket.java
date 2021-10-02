package dev.rlnt.energymeter.network;

import dev.rlnt.energymeter.meter.MeterContainer;
import dev.rlnt.energymeter.meter.MeterTile;
import dev.rlnt.energymeter.util.TypeEnums.SETTING;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

public class SettingUpdatePacket {

    private SETTING setting;

    public SettingUpdatePacket(final SETTING setting) {
        this.setting = setting;
    }

    private SettingUpdatePacket() {}

    static SettingUpdatePacket decode(final PacketBuffer buffer) {
        final SettingUpdatePacket packet = new SettingUpdatePacket();
        packet.setting = SETTING.values()[buffer.readInt()];
        return packet;
    }

    static void handle(final SettingUpdatePacket packet, final Supplier<NetworkEvent.Context> context) {
        final ServerPlayerEntity player = context.get().getSender();
        context.get().enqueueWork(() -> handlePacket(packet, player));
        context.get().setPacketHandled(true);
    }

    private static void handlePacket(final SettingUpdatePacket packet, @Nullable final ServerPlayerEntity player) {
        if (player != null && player.containerMenu instanceof MeterContainer) {
            final MeterTile tile = ((MeterContainer) player.containerMenu).getTile();
            tile.updateSetting(packet.setting);
            tile.update(false);
            tile.setChanged();
        }
    }

    void encode(final PacketBuffer buffer) {
        buffer.writeInt(setting.ordinal());
    }
}
