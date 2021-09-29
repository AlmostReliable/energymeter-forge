package dev.rlnt.energymeter.network;

import dev.rlnt.energymeter.meter.MeterContainer;
import dev.rlnt.energymeter.meter.MeterTile;
import dev.rlnt.energymeter.meter.SideConfiguration;
import dev.rlnt.energymeter.util.TypeEnums.BLOCK_SIDE;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Direction;
import net.minecraftforge.fml.network.NetworkEvent;

public class IOUpdatePacket {

    private Direction direction;
    private int[] sideConfig;

    public IOUpdatePacket(final SideConfiguration sideConfig, final BLOCK_SIDE side) {
        this.sideConfig = sideConfig.serialize();
        direction = sideConfig.getDirectionFromSide(side);
    }

    private IOUpdatePacket() {
        sideConfig = new int[12];
        direction = Direction.NORTH;
    }

    static IOUpdatePacket fromBytes(final PacketBuffer buffer) {
        final IOUpdatePacket packet = new IOUpdatePacket();
        packet.sideConfig = buffer.readVarIntArray();
        packet.direction = Direction.values()[buffer.readInt()];
        return packet;
    }

    static void handle(final IOUpdatePacket packet, final Supplier<NetworkEvent.Context> context) {
        final ServerPlayerEntity player = context.get().getSender();
        context.get().enqueueWork(() -> handlePacket(packet, player));
        context.get().setPacketHandled(true);
    }

    private static void handlePacket(final IOUpdatePacket packet, @Nullable final ServerPlayerEntity player) {
        if (player != null && player.containerMenu instanceof MeterContainer) {
            final MeterTile tile = ((MeterContainer) player.containerMenu).getTile();
            tile.getSideConfig().deserialize(packet.sideConfig);
            tile.update(true);
            tile.updateCache(packet.direction);
            tile.setChanged();
        }
    }

    void toBytes(final PacketBuffer buffer) {
        buffer.writeVarIntArray(sideConfig);
        buffer.writeInt(direction.ordinal());
    }
}
