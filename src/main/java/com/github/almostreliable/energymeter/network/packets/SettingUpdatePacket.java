package com.github.almostreliable.energymeter.network.packets;

import com.github.almostreliable.energymeter.meter.MeterContainer;
import com.github.almostreliable.energymeter.network.ClientToServerPacket;
import com.github.almostreliable.energymeter.util.TypeEnums.SETTING;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

import javax.annotation.Nullable;

public class SettingUpdatePacket extends ClientToServerPacket<SettingUpdatePacket> {

    private SETTING setting;

    public SettingUpdatePacket(SETTING setting) {
        this.setting = setting;
    }

    public SettingUpdatePacket() {}

    @Override
    public void encode(SettingUpdatePacket packet, FriendlyByteBuf buffer) {
        buffer.writeInt(packet.setting.ordinal());
    }

    @Override
    public SettingUpdatePacket decode(FriendlyByteBuf buffer) {
        return new SettingUpdatePacket(SETTING.values()[buffer.readInt()]);
    }

    @Override
    public void handlePacket(SettingUpdatePacket packet, @Nullable ServerPlayer player) {
        if (player != null && player.containerMenu instanceof MeterContainer menu) {
            var entity = menu.getEntity();
            var level = entity.getLevel();
            if (level == null || !level.isLoaded(entity.getBlockPos())) return;
            entity.updateSetting(packet.setting);
            entity.setChanged();
        }
    }
}
