package com.github.almostreliable.energymeter.network;

import com.github.almostreliable.energymeter.core.Constants.SYNC_FLAGS;
import com.github.almostreliable.energymeter.meter.MeterContainer;
import com.github.almostreliable.energymeter.meter.MeterTile;
import com.github.almostreliable.energymeter.util.TypeEnums.BLOCK_SIDE;
import com.github.almostreliable.energymeter.util.TypeEnums.IO_SETTING;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkEvent.Context;

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

    static IOUpdatePacket decode(PacketBuffer buffer) {
        IOUpdatePacket packet = new IOUpdatePacket();
        packet.side = BLOCK_SIDE.values()[buffer.readInt()];
        packet.setting = IO_SETTING.values()[buffer.readInt()];
        return packet;
    }

    static void handle(IOUpdatePacket packet, Supplier<? extends Context> context) {
        ServerPlayerEntity player = context.get().getSender();
        context.get().enqueueWork(() -> handlePacket(packet, player));
        context.get().setPacketHandled(true);
    }

    private static void handlePacket(IOUpdatePacket packet, @Nullable ServerPlayerEntity player) {
        if (player != null && player.containerMenu instanceof MeterContainer) {
            MeterTile tile = ((MeterContainer) player.containerMenu).getTile();
            World level = tile.getLevel();
            if (level == null || !level.isLoaded(tile.getBlockPos())) return;
            tile.getSideConfig().set(packet.side, packet.setting);
            tile.updateNeighbors();
            tile.updateCache(tile.getSideConfig().getDirectionFromSide(packet.side));
            tile.syncData(SYNC_FLAGS.SIDE_CONFIG);
            tile.setChanged();
        }
    }

    void encode(PacketBuffer buffer) {
        buffer.writeInt(side.ordinal());
        buffer.writeInt(setting.ordinal());
    }
}
