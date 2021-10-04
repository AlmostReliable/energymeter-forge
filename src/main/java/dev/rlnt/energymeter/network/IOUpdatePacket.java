package dev.rlnt.energymeter.network;

import dev.rlnt.energymeter.meter.MeterContainer;
import dev.rlnt.energymeter.meter.MeterTile;
import dev.rlnt.energymeter.meter.SideConfiguration;
import dev.rlnt.energymeter.network.PacketHandler.SyncFlags;
import dev.rlnt.energymeter.util.TypeEnums.BLOCK_SIDE;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Direction;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkEvent.Context;

public class IOUpdatePacket {

    private Direction direction;
    private int[] sideConfig;

    public IOUpdatePacket(final SideConfiguration sideConfig, final BLOCK_SIDE side) {
        this.sideConfig = sideConfig.serialize();
        direction = sideConfig.getDirectionFromSide(side);
    }

    private IOUpdatePacket() {
        sideConfig = new int[12];
        direction = null;
    }

    static IOUpdatePacket decode(final PacketBuffer buffer) {
        final IOUpdatePacket packet = new IOUpdatePacket();
        packet.sideConfig = buffer.readVarIntArray();
        packet.direction = Direction.values()[buffer.readInt()];
        return packet;
    }

    static void handle(final IOUpdatePacket packet, final Supplier<Context> context) {
        final ServerPlayerEntity player = context.get().getSender();
        context.get().enqueueWork(() -> handlePacket(packet, player));
        context.get().setPacketHandled(true);
    }

    private static void handlePacket(final IOUpdatePacket packet, @Nullable final ServerPlayerEntity player) {
        if (player != null && player.containerMenu instanceof MeterContainer) {
            final MeterTile tile = ((MeterContainer) player.containerMenu).getTile();
            final World level = tile.getLevel();
            if (level == null || !level.isLoaded(tile.getBlockPos())) return;
            tile.getSideConfig().deserialize(packet.sideConfig);
            tile.updateNeighbors();
            tile.updateCache(packet.direction);
            tile.syncData(SyncFlags.SIDE_CONFIG);
            tile.setChanged();
        }
    }

    void encode(final PacketBuffer buffer) {
        buffer.writeVarIntArray(sideConfig);
        buffer.writeInt(direction.ordinal());
    }
}
