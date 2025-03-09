package com.almostreliable.energymeter.network;

import com.almostreliable.energymeter.core.Constants.SyncFlags;
import com.almostreliable.energymeter.menu.MeterMenu;
import com.almostreliable.energymeter.util.TypeEnums.TextBox;
import com.almostreliable.energymeter.util.Utils;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record AccuracyUpdatePacket(TextBox identifier, int value) implements CustomPacketPayload {

    static final Type<AccuracyUpdatePacket> TYPE = new Type<>(Utils.getRL("accuracy_update"));
    static final StreamCodec<FriendlyByteBuf, AccuracyUpdatePacket> STREAM_CODEC = CustomPacketPayload.codec(
        AccuracyUpdatePacket::encode,
        AccuracyUpdatePacket::new
    );

    public AccuracyUpdatePacket(FriendlyByteBuf buffer) {
        this(TextBox.values()[buffer.readInt()], buffer.readInt());
    }

    public void encode(FriendlyByteBuf buffer) {
        buffer.writeInt(identifier.ordinal());
        buffer.writeInt(value);
    }

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(AccuracyUpdatePacket payload, IPayloadContext context) {
        if (context.player() instanceof ServerPlayer player) {
            var entity = ((MeterMenu) player.containerMenu).getEntity();
            var level = entity.getLevel();
            if (level == null || !level.isLoaded(entity.getBlockPos())) return;
            if (payload.identifier == TextBox.INTERVAL) {
                entity.setInterval(payload.value);
                entity.syncData(SyncFlags.INTERVAL);
            } else if (payload.identifier == TextBox.THRESHOLD) {
                entity.setThreshold(payload.value);
                entity.syncData(SyncFlags.THRESHOLD);
            }
            entity.setChanged();
        }
    }
}
