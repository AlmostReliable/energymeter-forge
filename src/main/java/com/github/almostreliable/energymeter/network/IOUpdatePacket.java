package com.github.almostreliable.energymeter.network;

import com.github.almostreliable.energymeter.core.Constants.SYNC_FLAGS;
import com.github.almostreliable.energymeter.meter.MeterContainer;
import com.github.almostreliable.energymeter.util.TypeEnums.BLOCK_SIDE;
import com.github.almostreliable.energymeter.util.TypeEnums.IO_SETTING;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent.Context;

import javax.annotation.Nullable;
import java.util.function.Supplier;

public class IOUpdatePacket {

    private BLOCK_SIDE side;
    private IO_SETTING setting;

    public IOUpdatePacket(BLOCK_SIDE side, IO_SETTING setting) {
        this.side = side;
        this.setting = setting;
    }

    private IOUpdatePacket() {}

    static IOUpdatePacket decode(FriendlyByteBuf buffer) {
        var packet = new IOUpdatePacket();
        packet.side = BLOCK_SIDE.values()[buffer.readInt()];
        packet.setting = IO_SETTING.values()[buffer.readInt()];
        return packet;
    }

    static void handle(IOUpdatePacket packet, Supplier<? extends Context> context) {
        var player = context.get().getSender();
        context.get().enqueueWork(() -> handlePacket(packet, player));
        context.get().setPacketHandled(true);
    }

    private static void handlePacket(IOUpdatePacket packet, @Nullable ServerPlayer player) {
        if (player != null && player.containerMenu instanceof MeterContainer menu) {
            var entity = menu.getEntity();
            var level = entity.getLevel();
            if (level == null || !level.isLoaded(entity.getBlockPos())) return;
            entity.getSideConfig().set(packet.side, packet.setting);
            entity.updateNeighbors();
            entity.updateCache(entity.getSideConfig().getDirectionFromSide(packet.side));
            entity.syncData(SYNC_FLAGS.SIDE_CONFIG);
            entity.setChanged();
        }
    }

    void encode(FriendlyByteBuf buffer) {
        buffer.writeInt(side.ordinal());
        buffer.writeInt(setting.ordinal());
    }
}
