package dev.rlnt.energymeter.network;

import dev.rlnt.energymeter.meter.MeterContainer;
import dev.rlnt.energymeter.meter.MeterEntity;
import dev.rlnt.energymeter.util.TypeEnums.BLOCK_SIDE;
import dev.rlnt.energymeter.util.TypeEnums.IO_SETTING;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraftforge.fmllegacy.network.NetworkEvent.Context;

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

    static void handle(IOUpdatePacket packet, Supplier<Context> context) {
        ServerPlayer player = context.get().getSender();
        context.get().enqueueWork(() -> handlePacket(packet, player));
        context.get().setPacketHandled(true);
    }

    private static void handlePacket(IOUpdatePacket packet, @Nullable ServerPlayer player) {
        if (player != null && player.containerMenu instanceof MeterContainer menu) {
            MeterEntity entity = menu.getEntity();
            Level level = entity.getLevel();
            if (level == null || !level.isLoaded(entity.getBlockPos())) return;
            entity.getSideConfig().set(packet.side, packet.setting);
            entity.update(true);
            entity.updateCache(entity.getSideConfig().getDirectionFromSide(packet.side));
            entity.setChanged();
        }
    }

    void encode(FriendlyByteBuf buffer) {
        buffer.writeInt(side.ordinal());
        buffer.writeInt(setting.ordinal());
    }
}
