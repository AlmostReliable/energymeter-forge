package com.github.almostreliable.energymeter.network;

import com.github.almostreliable.energymeter.meter.MeterContainer;
import com.github.almostreliable.energymeter.util.TypeEnums.SETTING;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent.Context;

import javax.annotation.Nullable;
import java.util.function.Supplier;

public class SettingUpdatePacket {

    private SETTING setting;

    public SettingUpdatePacket(SETTING setting) {
        this.setting = setting;
    }

    private SettingUpdatePacket() {}

    static SettingUpdatePacket decode(FriendlyByteBuf buffer) {
        var packet = new SettingUpdatePacket();
        packet.setting = SETTING.values()[buffer.readInt()];
        return packet;
    }

    static void handle(SettingUpdatePacket packet, Supplier<? extends Context> context) {
        var player = context.get().getSender();
        context.get().enqueueWork(() -> handlePacket(packet, player));
        context.get().setPacketHandled(true);
    }

    private static void handlePacket(SettingUpdatePacket packet, @Nullable ServerPlayer player) {
        if (player != null && player.containerMenu instanceof MeterContainer menu) {
            var entity = menu.getEntity();
            var level = entity.getLevel();
            if (level == null || !level.isLoaded(entity.getBlockPos())) return;
            entity.updateSetting(packet.setting);
            entity.setChanged();
        }
    }

    void encode(FriendlyByteBuf buffer) {
        buffer.writeInt(setting.ordinal());
    }
}
