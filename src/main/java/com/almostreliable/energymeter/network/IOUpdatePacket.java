package com.almostreliable.energymeter.network;

import com.almostreliable.energymeter.core.Constants.SyncFlags;
import com.almostreliable.energymeter.menu.MeterMenu;
import com.almostreliable.energymeter.util.TypeEnums.BlockSide;
import com.almostreliable.energymeter.util.TypeEnums.IoSetting;
import com.almostreliable.energymeter.util.Utils;

import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import io.netty.buffer.ByteBuf;

public record IOUpdatePacket(BlockSide side, IoSetting setting) implements CustomPacketPayload {

    static final Type<IOUpdatePacket> TYPE = new Type<>(Utils.getRL("io_update"));
    static final StreamCodec<ByteBuf, IOUpdatePacket> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.VAR_INT, p -> p.side.ordinal(),
        ByteBufCodecs.VAR_INT, p -> p.setting.ordinal(),
        IOUpdatePacket::new
    );

    public IOUpdatePacket(Integer sideIndex, Integer settingsIndex) {
        this(BlockSide.values()[sideIndex], IoSetting.values()[settingsIndex]);
    }

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(IOUpdatePacket payload, IPayloadContext context) {
        if (context.player().containerMenu instanceof MeterMenu menu) {
            var entity = menu.getEntity();
            var level = entity.getLevel();
            if (level == null || !level.isLoaded(entity.getBlockPos())) return;
            entity.getSideConfig().set(payload.side, payload.setting);
            entity.updateNeighbors();
            entity.updateCache(entity.getSideConfig().getDirectionFromSide(payload.side));
            entity.syncData(SyncFlags.SIDE_CONFIG);
            entity.setChanged();
        }
    }
}
