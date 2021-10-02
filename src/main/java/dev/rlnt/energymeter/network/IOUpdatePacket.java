package dev.rlnt.energymeter.network;

import dev.rlnt.energymeter.meter.MeterContainer;
import dev.rlnt.energymeter.meter.MeterEntity;
import dev.rlnt.energymeter.meter.SideConfiguration;
import dev.rlnt.energymeter.util.TypeEnums.BLOCK_SIDE;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.fmllegacy.network.NetworkEvent.Context;

public class IOUpdatePacket {

    private Direction direction;
    private int[] sideConfig;

    public IOUpdatePacket(SideConfiguration sideConfig, BLOCK_SIDE side) {
        this.sideConfig = sideConfig.serialize();
        direction = sideConfig.getDirectionFromSide(side);
    }

    private IOUpdatePacket() {
        sideConfig = new int[12];
        direction = null;
    }

    static IOUpdatePacket decode(FriendlyByteBuf buffer) {
        IOUpdatePacket packet = new IOUpdatePacket();
        packet.sideConfig = buffer.readVarIntArray();
        packet.direction = Direction.values()[buffer.readInt()];
        return packet;
    }

    static void handle(IOUpdatePacket packet, Supplier<Context> context) {
        ServerPlayer player = context.get().getSender();
        context.get().enqueueWork(() -> handlePacket(packet, player));
        context.get().setPacketHandled(true);
    }

    private static void handlePacket(IOUpdatePacket packet, @Nullable ServerPlayer player) {
        if (player != null && player.containerMenu instanceof MeterContainer menu) {
            MeterEntity tile = menu.getEntity();
            tile.getSideConfig().deserialize(packet.sideConfig);
            tile.update(true);
            tile.updateCache(packet.direction);
            tile.setChanged();
        }
    }

    void encode(FriendlyByteBuf buffer) {
        buffer.writeVarIntArray(sideConfig);
        buffer.writeInt(direction.ordinal());
    }
}
