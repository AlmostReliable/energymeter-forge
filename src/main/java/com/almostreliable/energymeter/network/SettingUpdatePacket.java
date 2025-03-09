package com.almostreliable.energymeter.network;

import com.almostreliable.energymeter.menu.MeterMenu;
import com.almostreliable.energymeter.util.TypeEnums.Setting;
import com.almostreliable.energymeter.util.Utils;

import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import io.netty.buffer.ByteBuf;

public record SettingUpdatePacket(Setting setting) implements CustomPacketPayload {

    static final Type<SettingUpdatePacket> TYPE = new Type<>(Utils.getRL("setting_update"));
    static final StreamCodec<ByteBuf, SettingUpdatePacket> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.VAR_INT, p -> p.setting.ordinal(),
        s -> new SettingUpdatePacket(Setting.values()[s])
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(SettingUpdatePacket payload, IPayloadContext context) {
        if (context.player().containerMenu instanceof MeterMenu menu) {
            var entity = menu.getEntity();
            var level = entity.getLevel();
            if (level == null || !level.isLoaded(entity.getBlockPos())) return;
            entity.updateSetting(payload.setting);
            entity.setChanged();
        }
    }
}
